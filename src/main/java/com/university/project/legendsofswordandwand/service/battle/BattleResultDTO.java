package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import java.util.Collections;
import java.util.List;

/**
 * DTO used by {@link IBattleService} to expose battle result data needed by the result page.
 */
public class BattleResultDTO {

  private final int rewardGold;
  private final List<String> rewardRecipients;
  private final List<Hero> levelUpHeroes;
  private final boolean rewardsApplied;

  public BattleResultDTO(
      int rewardGold,
      List<String> rewardRecipients,
      List<Hero> levelUpHeroes,
      boolean rewardsApplied) {
    this.rewardGold = rewardGold;
    this.rewardRecipients = rewardRecipients;
    this.levelUpHeroes = levelUpHeroes;
    this.rewardsApplied = rewardsApplied;
  }

  public int getRewardGold() {
    return rewardGold;
  }

  public List<String> getRewardRecipients() {
    return rewardRecipients;
  }

  public List<Hero> getLevelUpHeroes() {
    return levelUpHeroes;
  }

  public boolean isRewardsApplied() {
    return rewardsApplied;
  }

  public static BattleResultDTO empty() {
    return new BattleResultDTO(0, Collections.emptyList(), Collections.emptyList(), false);
  }
}
