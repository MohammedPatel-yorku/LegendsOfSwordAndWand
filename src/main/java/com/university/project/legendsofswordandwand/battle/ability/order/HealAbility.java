package com.university.project.legendsofswordandwand.battle.ability.order;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;

import java.util.Comparator;
import java.util.List;

public class HealAbility implements Ability {

    private final HybridClass hybridClass;

    public HealAbility(HybridClass hybridClass) {
        this.hybridClass = hybridClass;
    }

    @Override
    public int getManaCost() {
        return 35;
    }

    @Override
    public void execute(BattleUnit caster, BattleUnit target, List<BattleUnit> allies, List<BattleUnit> enemies, BattleState state) {

        double multiplier = (hybridClass == HybridClass.PROPHET) ? 2.0 : 1.0;

        if (hybridClass == HybridClass.PRIEST) {

            allies.forEach(u -> applyHeal(u.getHero(), multiplier, state));
        } else {

            allies.stream()
                    .min(Comparator.comparingInt(unit -> unit.getHero().getHealth()))
                    .ifPresent(unit -> applyHeal(unit.getHero(), multiplier, state));
        }
    }

    private void applyHeal(HeroSnapshot hero, double multiplier, BattleState state) {

        int heal = (int) (hero.getMaxHealth() * 0.25 * multiplier);
        int before = hero.getHealth();
        hero.setHealth(Math.min(hero.getMaxHealth(), hero.getHealth() + heal));
        int actual = hero.getHealth() - before;
        state.log("  ✦ Heal restores " + actual + " HP to " + hero.getName()
                + " → " + hero.getHealth() + " HP");
    }
}
