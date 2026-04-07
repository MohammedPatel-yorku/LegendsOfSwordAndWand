package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.battle.BattleState;
import java.util.Map;

public interface IBattleRewardService {
    Map<String, Object> awardBattleRewards(BattleState state);
    void applyBattleLoss(BattleState state);
}