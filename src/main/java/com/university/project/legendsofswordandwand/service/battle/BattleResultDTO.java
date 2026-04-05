package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.model.Hero;

import java.util.Collections;
import java.util.List;

/**
 * DTO used by {@link IBattleService} to expose battle result data needed by the result page.
 */
public record BattleResultDTO(int rewardGold, List<String> rewardRecipients, List<Hero> levelUpHeroes,
                              boolean rewardsApplied) {

    /**
     * Returns an empty {@link BattleResultDTO} object.
     *
     * @return empty {@link BattleResultDTO}
     */
    public static BattleResultDTO empty() {
        return new BattleResultDTO(0, Collections.emptyList(), Collections.emptyList(), false);
    }
}