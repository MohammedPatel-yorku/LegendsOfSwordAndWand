package com.university.project.legendsofswordandwand.battle.ability;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.model.Hero;

public final class AbilityHelper {

    private AbilityHelper() {}

    public static void applyDamage(HeroSnapshot attacker, BattleUnit target, int raw, BattleState state) {
        int shieldValue = state.getShield(target.getBattleId());
        boolean fireShield = shieldValue < 0;
        int absShield = Math.abs(shieldValue);

        int absorbed = Math.min(absShield, raw);
        int piercing = raw - absorbed;
        int remaining = absShield - absorbed;

        state.setShield(target.getBattleId(), fireShield ? -remaining : remaining);
        target.getHero().setHealth(Math.max(0, target.getHero().getHealth() - piercing));

        if (fireShield && absorbed > 0 && attacker != null) {
            int reflected = (int) (absorbed * 0.10);
            attacker.setHealth(Math.max(0, attacker.getHealth() - reflected));
        }
    }

    public static int calculateDamage(BattleUnit attacker, BattleUnit defender) {
        return Math.max(0, attacker.getHero().getAttack() - defender.getHero().getDefense());
    }
}
