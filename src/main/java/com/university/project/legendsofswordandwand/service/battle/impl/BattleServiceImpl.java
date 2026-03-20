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
import java.util.*;
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
      if (hero.isTemporary()) continue; // don't include unchosen recruits
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
  public BattleState executePlayerAction(
      BattleState state, ActionType actionType, Long targetBattleId, Integer abilityIndex) {

    if (state.isOver() || !state.isPlayerTurn()) return state;

    BattleUnit actor = state.getActiveUnit();
    if (actor == null || !actor.isAlive()) return advanceTurn(state);

    if (state.isStunned(actor.getBattleId())) {
      state.tickStuns();
      return advanceTurn(state);
    }

    state.log(
        "► "
            + actor.getHero().getName()
            + " [HP:"
            + actor.getHero().getHealth()
            + "] acts: "
            + actionType);

    BattleUnit target = targetBattleId != null ? state.findUnit(targetBattleId) : null;

    switch (actionType) {
      case ATTACK -> {
        if (target != null) executeAttack(actor, target, state);
      }
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
      case CAST -> {
        List<BattleUnit> allies = state.getLivingPlayerHeroes();
        List<BattleUnit> enemies = state.getLivingEnemyHeroes();
        abilityExecutor.executeAbility(
            actor, target, allies, enemies, state, abilityIndex != null ? abilityIndex : 0);
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
    if (!targets.isEmpty()) {
      decideEnemyAction(actor, targets, state);
    } else {
      executeDefend(actor.getHero());
      state.log("  " + actor.getHero().getName() + " defends");
    }

    state.setStatus(checkBattleStatus(state));
    if (!state.isOver()) advanceTurn(state);
    return state;
  }

  private void decideEnemyAction(BattleUnit actor, List<BattleUnit> targets, BattleState state) {
    String name = actor.getHero().getName();

    switch (name) {
      case "Skeleton", "Witch", "Shadow" -> {
        // Glass cannon — always attacks weakest, no spread needed (they die fast anyway)
        BattleUnit target =
            targets.stream()
                .min(Comparator.comparingInt(u -> u.getHero().getHealth()))
                .orElse(targets.get(0));
        executeAttack(actor, target, state);
      }
      case "Orc", "Dark Knight" -> {
        // Brute — 60% attack highest attack hero, 40% pick random to spread pressure
        BattleUnit target =
            random.nextInt(100) < 60
                ? targets.stream()
                    .max(Comparator.comparingInt(u -> u.getHero().getAttack()))
                    .orElse(targets.get(0))
                : targets.get(random.nextInt(targets.size()));
        executeAttack(actor, target, state);
      }
      case "Goblin", "Vampire" -> {
        // Swift — 75% attack lowest defense, 25% wait
        if (random.nextInt(100) < 75) {
          BattleUnit target =
              targets.stream()
                  .min(Comparator.comparingInt(u -> u.getHero().getDefense()))
                  .orElse(targets.get(0));
          executeAttack(actor, target, state);
        } else {
          state.getTurnQueue().addLast(actor.getBattleId());
          state.log("  " + actor.getHero().getName() + " waits");
        }
      }
      case "Troll" -> {
        // Tank — defends if hurt, otherwise 50% attack highest HP, 50% random
        boolean hurt = actor.getHero().getHealth() < actor.getHero().getMaxHealth() / 4;
        if (hurt && random.nextInt(100) < 40) {
          executeDefend(actor.getHero());
          state.log("  " + actor.getHero().getName() + " defends");
        } else {
          BattleUnit target =
              random.nextInt(100) < 50
                  ? targets.stream()
                      .max(Comparator.comparingInt(u -> u.getHero().getHealth()))
                      .orElse(targets.get(0))
                  : targets.get(random.nextInt(targets.size()));
          executeAttack(actor, target, state);
        }
      }
      default -> {
        // Balanced (Bandit, Wyvern) — always random target
        if (random.nextInt(100) < 85) {
          BattleUnit target = targets.get(random.nextInt(targets.size()));
          executeAttack(actor, target, state);
        } else {
          executeDefend(actor.getHero());
          state.log("  " + actor.getHero().getName() + " defends");
        }
      }
    }
  }

  @Override
  public BattleStatus checkBattleStatus(BattleState state) {
    if (state.getLivingEnemyHeroes().isEmpty()) return BattleStatus.PLAYER_WIN;
    if (state.getLivingPlayerHeroes().isEmpty()) return BattleStatus.PLAYER_LOSE;
    return BattleStatus.IN_PROGRESS;
  }

  @Override
  public Map<String, Object> awardBattleRewards(BattleState state) {
    List<BattleUnit> living = state.getLivingPlayerHeroes();
    if (living.isEmpty()) return Map.of("xpEach", 0, "gold", 0, "recipients", List.of());

    int totalXp = state.getEnemyUnits().stream().mapToInt(u -> 50 * u.getHero().getLevel()).sum();
    int xpEach = totalXp / living.size();
    int remainder = totalXp % living.size();

    List<String> recipients = new ArrayList<>();
    for (int i = 0; i < living.size(); i++) {
      int xp = xpEach + (i == 0 ? remainder : 0);
      heroService.addExperience(living.get(i).getHero().getId(), xp);
      recipients.add(living.get(i).getHero().getName() + " +" + xp + " XP");
    }

    int gold = state.getEnemyUnits().stream().mapToInt(u -> 75 * u.getHero().getLevel()).sum();
    if (state.getCampaignId() != null) {
      Party party = partyManagementService.getActiveParty(state.getCampaignId());
      partyManagementService.addGold(party.getId(), gold);
    }

    state
        .getPlayerUnits()
        .forEach(
            u ->
                heroService
                    .findById(u.getHero().getId())
                    .ifPresent(
                        hero -> {
                          hero.setHealth(u.getHero().getHealth());
                          hero.setMana(u.getHero().getMana());
                          heroService.save(hero);
                        }));

    Map<String, Object> rewards = new HashMap<>();
    rewards.put("gold", gold);
    rewards.put("recipients", recipients);
    return rewards;
  }

  @Override
  public void applyBattleLoss(BattleState state) {
    // Write snapshot XP penalties back to DB
    state
        .getPlayerUnits()
        .forEach(
            u -> {
              int prevThreshold =
                  u.getHero().getExperienceToNextLevel()
                      - (500
                          + 75 * u.getHero().getLevel()
                          + 20 * u.getHero().getLevel() * u.getHero().getLevel());
              int xpInCurrentLevel = Math.max(0, u.getHero().getExperience() - prevThreshold);
              int penalty = (int) (xpInCurrentLevel * 0.30);
              int newXp = Math.max(prevThreshold, u.getHero().getExperience() - penalty);
              // Load fresh entity and update
              heroService
                  .findById(u.getHero().getId())
                  .ifPresent(
                      hero -> {
                        hero.setExperience(newXp);
                        hero.setHealth(u.getHero().getHealth());
                        hero.setMana(u.getHero().getMana());
                        heroService.save(hero);
                      });
            });

    if (state.getCampaignId() != null) {
      Party party = partyManagementService.getActiveParty(state.getCampaignId());
      partyManagementService.deductGold(party.getId(), (int) (party.getGold() * 0.10));
    }
  }

  private void executeAttack(BattleUnit attacker, BattleUnit defender, BattleState state) {
    int damage =
        damageCalculator.calculateDamage(
            attacker.getHero().getAttack(), defender.getHero().getDefense());
    int hpBefore = defender.getHero().getHealth();
    AbilityHelper.applyDamage(attacker.getHero(), defender, damage, state);
    int actualDamage = hpBefore - defender.getHero().getHealth();

    state.log(
        "  "
            + attacker.getHero().getName()
            + " attacks "
            + defender.getHero().getName()
            + " for "
            + damage
            + " dmg → "
            + defender.getHero().getHealth()
            + " HP left"
            + (actualDamage < damage ? " (shield absorbed " + (damage - actualDamage) + ")" : ""));

    HybridClass hybrid = attacker.getHero().getHybridClass();
    if (hybrid == HybridClass.ROGUE) abilityExecutor.maybeSneak(attacker, defender, state);
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

    state.getTurnQueue().clear();

    List<BattleUnit> living = new ArrayList<>();
    living.addAll(state.getLivingPlayerHeroes());
    living.addAll(state.getLivingEnemyHeroes());
    living.sort(
        (a, b) -> {
          int lvlDiff = b.getHero().getLevel() - a.getHero().getLevel();
          return lvlDiff != 0 ? lvlDiff : b.getHero().getAttack() - a.getHero().getAttack();
        });
    living.forEach(u -> state.getTurnQueue().add(u.getBattleId()));
  }
}
