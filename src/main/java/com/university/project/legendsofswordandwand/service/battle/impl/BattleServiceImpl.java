package com.university.project.legendsofswordandwand.service.battle.impl;

import com.university.project.legendsofswordandwand.battle.*;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
class BattleServiceImpl implements IBattleService {

  private final DamageCalculator damageCalculator;
  private final AbilityExecutor abilityExecutor;
  private final EnemyGenerator enemyGenerator;
  private final IHeroService heroService;
  private final IPartyManagementService partyManagementService;
  private final Random random = new Random();

  @Override
  public BattleState initializePvEBattle(Long campaignId, int playerCumulativeLevel) {
    Party party = partyManagementService.getActiveParty(campaignId);
    List<Hero> enemies = enemyGenerator.generate(playerCumulativeLevel, party.getHeroes().size());

    BattleState state = new BattleState();
    state.setCampaignId(campaignId);

    for (Hero hero : party.getHeroes()) {
      if (hero.isTemporary()) continue;   // don't include unchosen recruits
      state.getPlayerUnits().add(new BattleUnit(hero.getId(), new HeroSnapshot(hero), false));
    }

    long enemyId = -1L;
    for (Hero enemy : enemies) {
      state.getEnemyUnits().add(new BattleUnit(enemyId--, new HeroSnapshot(enemy), true));
    }

    buildTurnQueue(state);
    state.setStatus(BattleStatus.IN_PROGRESS);
    return state;
  }

  @Override
  public BattleState executePlayerAction(BattleState state, ActionType actionType,
                                         Long targetBattleId, Integer abilityIndex) {

    if (state.isOver() || !state.isPlayerTurn()) return state;

    BattleUnit actor = state.getActiveUnit();
    if (actor == null || !actor.isAlive()) return advanceTurn(state);

    if (state.isStunned(actor.getBattleId())) {
      state.tickStuns();
      return advanceTurn(state);
    }

    state.log("► " + actor.getHero().getName() + " [HP:" + actor.getHero().getHealth() + "] acts: " + actionType);

    BattleUnit target = targetBattleId != null ? state.findUnit(targetBattleId) : null;

    switch (actionType) {
      case ATTACK -> { if (target != null) executeAttack(actor, target, state); }
      case DEFEND -> {
        executeDefend(actor.getHero());
        state.log("  " + actor.getHero().getName() + " defends (+10 HP, +5 MP)");
      }
      case WAIT -> {
        state.log("  " + actor.getHero().getName() + " waits");
        state.getTurnQueue().addLast(actor.getBattleId());
        state.setActiveUnitBattleId(null);
        state.setStatus(checkBattleStatus(state));
        return advanceTurn(state);
      }
      case CAST   -> {
        List<BattleUnit> allies  = state.getLivingPlayerHeroes();
        List<BattleUnit> enemies = state.getLivingEnemyHeroes();
        abilityExecutor.executeAbility(actor, target, allies, enemies, state,
                abilityIndex != null ? abilityIndex : 0);
      }
    }

    state.setStatus(checkBattleStatus(state));
    if (!state.isOver()) advanceTurn(state);
    return state;
  }

  @Override
  public BattleState executeEnemyTurn(BattleState state) {
    if (state.isOver() || state.isPlayerTurn()) return state;

    BattleUnit actor = state.getActiveUnit();
    if (actor == null || !actor.isAlive()) return advanceTurn(state);

    if (state.isStunned(actor.getBattleId())) {
      state.tickStuns();
      return advanceTurn(state);
    }

    List<BattleUnit> targets = state.getLivingPlayerHeroes();
    if (!targets.isEmpty() && random.nextInt(100) < 90) {
      BattleUnit weakest = targets.stream()
              .min((a, b) -> Integer.compare(a.getHero().getHealth(), b.getHero().getHealth()))
              .orElse(targets.get(0));
      executeAttack(actor, weakest, state);
    } else {
      executeDefend(actor.getHero());
      state.log("  " + actor.getHero().getName() + " defends");
    }

    state.setStatus(checkBattleStatus(state));
    if (!state.isOver()) advanceTurn(state);
    return state;
  }

  @Override
  public BattleStatus checkBattleStatus(BattleState state) {
    if (state.getLivingEnemyHeroes().isEmpty()) return BattleStatus.PLAYER_WIN;
    if (state.getLivingPlayerHeroes().isEmpty()) return BattleStatus.PLAYER_LOSE;
    return BattleStatus.IN_PROGRESS;
  }

  @Override
  public void awardBattleRewards(BattleState state) {
    List<BattleUnit> living = state.getLivingPlayerHeroes();
    if (living.isEmpty()) return;

    int totalXp = state.getEnemyUnits().stream()
            .mapToInt(u -> 75 * u.getHero().getLevel()).sum();
    int xpEach = totalXp / living.size();
    int remainder = totalXp % living.size();
    for (int i = 0; i <living.size(); i++) {

      int xp = xpEach + (i == 0 ? remainder : 0);
      heroService.addExperience(living.get(i).getHero().getId(), xp);
    }

    int gold = state.getEnemyUnits().stream()
            .mapToInt(u -> 75 * u.getHero().getLevel()).sum();
    Party party = partyManagementService.getActiveParty(state.getCampaignId());
    partyManagementService.addGold(party.getId(), gold);

    state.getPlayerUnits().forEach(u -> heroService.findById(u.getHero().getId()).ifPresent(hero -> {
      hero.setHealth(u.getHero().getHealth());
      hero.setMana(u.getHero().getMana());
      heroService.save(hero);
    }));
  }

  @Override
  public void applyBattleLoss(BattleState state) {
    // Write snapshot XP penalties back to DB
    state.getPlayerUnits().forEach(u -> {
      int penalty = (int) (u.getHero().getExperience() * 0.30);
      int newXp = Math.max(0, u.getHero().getExperience() - penalty);
      // Load fresh entity and update
      heroService.findById(u.getHero().getId()).ifPresent(hero -> {
        hero.setExperience(newXp);
        hero.setHealth(u.getHero().getHealth());
        hero.setMana(u.getHero().getMana());
        heroService.save(hero);
      });
    });

    Party party = partyManagementService.getActiveParty(state.getCampaignId());
    partyManagementService.deductGold(party.getId(), (int) (party.getGold() * 0.10));
  }

  private void executeAttack(BattleUnit attacker, BattleUnit defender, BattleState state) {
    int damage = damageCalculator.calculateDamage(
            attacker.getHero().getAttack(), defender.getHero().getDefense());
    int hpBefore = defender.getHero().getHealth();
    AbilityHelper.applyDamage(attacker.getHero(), defender, damage, state);
    int actualDamage = hpBefore - defender.getHero().getHealth();

    state.log("  " + attacker.getHero().getName() + " attacks " + defender.getHero().getName()
            + " for " + damage + " dmg → " + defender.getHero().getHealth() + " HP left"
            + (actualDamage < damage ? " (shield absorbed " + (damage - actualDamage) + ")" : ""));

    HybridClass hybrid = attacker.getHero().getHybridClass();
    if (hybrid == HybridClass.ROGUE)   abilityExecutor.maybeSneak(attacker, defender, state);
    if (hybrid == HybridClass.WARLOCK) abilityExecutor.applyManaBurn(defender);
  }

  private void executeDefend(HeroSnapshot hero) {
    hero.setHealth(Math.min(hero.getMaxHealth(), hero.getHealth() + 10));
    hero.setMana(Math.min(hero.getMaxMana(), hero.getMana() + 5));
  }

  private BattleState advanceTurn(BattleState state) {
    Long next = state.getTurnQueue().pollFirst();

    while (next != null) {
      BattleUnit unit = state.findUnit(next);
      if (unit != null && unit.isAlive()) break;
      next = state.getTurnQueue().pollFirst();
    }

    if (next == null) {
      state.tickStuns();
      refillTurnQueue(state);
      next = state.getTurnQueue().pollFirst();
      while (next != null) {
        BattleUnit unit = state.findUnit(next);
        if (unit != null && unit.isAlive()) break;
        next = state.getTurnQueue().pollFirst();
      }
    }

    if (next != null) {
      state.setActiveUnitBattleId(next);
      state.setPlayerTurn(!state.getActiveUnit().isEnemy());
    }
    return state;
  }

  private void buildTurnQueue(BattleState state) {

    refillTurnQueue(state);

    state.setActiveUnitBattleId(state.getTurnQueue().pollFirst());
    state.setPlayerTurn(!state.getActiveUnit().isEnemy());
  }

  private void refillTurnQueue(BattleState state) {

    List<BattleUnit> living = new ArrayList<>();
    living.addAll(state.getLivingPlayerHeroes());
    living.addAll(state.getLivingEnemyHeroes());
    living.sort((a, b) -> {
      int lvlDiff = b.getHero().getLevel() - a.getHero().getLevel();
      return lvlDiff != 0 ? lvlDiff : b.getHero().getAttack() - a.getHero().getAttack();
    });
    living.forEach(u -> state.getTurnQueue().add(u.getBattleId()));
  }
}