package com.university.project.legendsofswordandwand.battle.ability.warrior;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;

import java.util.List;
import java.util.Random;

public class BerserkerAbility implements Ability {

    private final HybridClass hybridClass;
    private final Random random;

    public BerserkerAbility(HybridClass hybridClass, Random random) {

        this.hybridClass = hybridClass;
        this.random = random;
    }

    @Override
    public int getManaCost() {
        return 60;
    }

    @Override
    public void execute(BattleUnit caster, BattleUnit target, List<BattleUnit> allies, List<BattleUnit> enemies, BattleState state) {

        if (target == null) return;

        HeroSnapshot casterHero = caster.getHero();

        if (hybridClass == HybridClass.PALADIN) {

            int heal = (int) (casterHero.getMaxHealth() * 0.10);
            casterHero.setHealth(Math.min(casterHero.getMaxHealth(), casterHero.getHealth() + heal));
        }

        int primaryDamage = AbilityHelper.calculateDamage(caster, target);
        AbilityHelper.applyDamage(casterHero, target, primaryDamage, state);
        maybeStun(target, state);

        enemies.stream()
                .filter(unit -> unit.getBattleId() != target.getBattleId())
                .limit(2)
                .forEach(unit -> {
                    AbilityHelper.applyDamage(casterHero, unit, primaryDamage / 4, state);
                    maybeStun(unit, state);
                });
    }

    private void maybeStun(BattleUnit unit, BattleState state) {

        if (hybridClass == HybridClass.KNIGHT && random.nextBoolean()) {

            state.applyStun(unit.getBattleId());
        }
    }
}
