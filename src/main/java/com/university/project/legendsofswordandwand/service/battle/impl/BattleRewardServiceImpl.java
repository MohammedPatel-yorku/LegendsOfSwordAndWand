package com.university.project.legendsofswordandwand.service.battle.impl;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroStatCalculator;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.service.battle.IBattleRewardService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BattleRewardServiceImpl implements IBattleRewardService {

  // ── Smell 2 fix: named constants replacing magic number literals ───────────
  private static final int XP_PER_ENEMY_LEVEL = 50;
  private static final int GOLD_PER_ENEMY_LEVEL = 75;
  private static final double XP_LOSS_PENALTY_FRACTION = 0.30;
  private static final double GOLD_LOSS_PENALTY_FRACTION = 0.10;

  private final IHeroService heroService;
  private final IPartyManagementService partyManagementService;
  // Smell 3 fix: injected so applyBattleLoss() can call getExpStepForLevel()
  private final HeroStatCalculator heroStatCalculator;

  /**
   * Awards XP and gold to surviving player heroes after a victory.
   *
   * <p>Total XP is split evenly among living heroes, with any remainder awarded to the first hero.
   * Gold is calculated from enemy levels and added to the party treasury. Each surviving hero's HP
   * and mana snapshots are written back to the database.
   *
   * @param state the current {@link BattleState}
   * @return a map containing {@code "gold"} (int) and {@code "recipients"} (list of reward strings)
   */
  @Override
  public Map<String, Object> awardBattleRewards(BattleState state) {
    List<BattleUnit> living = state.getLivingPlayerHeroes();
    if (living.isEmpty()) return Map.of("xpEach", 0, "gold", 0, "recipients", List.of());

    int totalXp =
        state.getEnemyUnits().stream()
            .mapToInt(u -> XP_PER_ENEMY_LEVEL * u.getHero().getLevel())
            .sum();
    int xpEach = totalXp / living.size();
    int remainder = totalXp % living.size();

    List<String> recipients = new ArrayList<>();
    for (int i = 0; i < living.size(); i++) {
      int xp = xpEach + (i == 0 ? remainder : 0);
      heroService.addExperience(living.get(i).getHero().getId(), xp);
      recipients.add(living.get(i).getHero().getName() + " +" + xp + " XP");
    }

    int gold =
        state.getEnemyUnits().stream()
            .mapToInt(u -> GOLD_PER_ENEMY_LEVEL * u.getHero().getLevel())
            .sum();
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

  /**
   * Applies penalties to all player heroes after a battle loss.
   *
   * <p>Each hero loses 30% of the XP accumulated within their current level, floored at the start
   * of that level. HP and mana snapshots are written back to the database. The party also loses 10%
   * of their current gold.
   *
   * @param state the current {@link BattleState}
   */
  @Override
  public void applyBattleLoss(BattleState state) {

    state
        .getPlayerUnits()
        .forEach(
            u -> {
              int prevThreshold =
                  u.getHero().getExperienceToNextLevel()
                      - heroStatCalculator.getExpStepForLevel(u.getHero().getLevel());
              int xpInCurrentLevel = Math.max(0, u.getHero().getExperience() - prevThreshold);
              int penalty = (int) (xpInCurrentLevel * XP_LOSS_PENALTY_FRACTION);
              int newXp = Math.max(prevThreshold, u.getHero().getExperience() - penalty);
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
      partyManagementService.deductGold(
          party.getId(), (int) (party.getGold() * GOLD_LOSS_PENALTY_FRACTION));
    }
  }
}
