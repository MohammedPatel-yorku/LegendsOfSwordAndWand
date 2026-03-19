package com.university.project.legendsofswordandwand.battle.ability.order;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;

import java.util.List;

public class ProtectAbility implements Ability {

    private final HybridClass hybridClass;

    public ProtectAbility(HybridClass hybridClass) {
        this.hybridClass = hybridClass;
    }

    @Override
    public int getManaCost() { return 25; }

    @Override
    public void execute(
            BattleUnit caster,
            BattleUnit target,
            List<BattleUnit> allies,
            List<BattleUnit> enemies,
            BattleState state) {

        double multiplier = (hybridClass == HybridClass.PROPHET) ? 2.0 : 1.0;

        for (BattleUnit ally : allies) {

            int shield = (int) (ally.getHero().getMaxHealth() * 0.10 * multiplier);
            int current = state.getShield(ally.getBattleId());

            state.setShield(ally.getBattleId(), Math.max(current, shield));
            state.log("  ✦ Protect shields " + ally.getHero().getName() + " for " + shield + " HP");
        }
    }
}
