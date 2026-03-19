package com.university.project.legendsofswordandwand.battle.ability.mage;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;

import java.util.List;

public class ReplenishAbility implements Ability {

    private final HybridClass hybridClass;

    public ReplenishAbility(HybridClass hybridClass) {

        this.hybridClass = hybridClass;
    }

    @Override
    public int getManaCost() {
        return (hybridClass == HybridClass.WIZARD) ? 40 : 80;
    }

    @Override
    public void execute(BattleUnit caster, BattleUnit target, List<BattleUnit> allies, List<BattleUnit> enemies, BattleState state) {

        double multiplier = (hybridClass == HybridClass.PROPHET) ? 2.0 : 1.0;
        int allyRestore = (int) (30 * multiplier);
        int selfRestore = (int) (60 * multiplier);

        for (BattleUnit ally : allies) {

            HeroSnapshot hero = ally.getHero();
            int restore = (ally.getBattleId() == caster.getBattleId()) ? selfRestore : allyRestore;
            hero.setMana(Math.min(hero.getMaxMana(), hero.getMana() + restore));
            state.log("  ✦ Replenish restores " + restore + " MP to " + hero.getName()
                    + " → " + hero.getMana() + " MP");
        }
    }
}
