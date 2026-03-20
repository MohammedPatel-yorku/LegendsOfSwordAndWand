package com.university.project.legendsofswordandwand.battle.ability;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;

/**
 * Utility class providing shared damage calculation and application logic for abilities.
 *
 * <p>This class is not instantiable and all methods are static.
 */
public final class AbilityHelper {

    private AbilityHelper() {}

    /**
     * Applies raw damage from an attacker to a target, accounting for active shields.
     *
     * <p>If the target has an active shield, incoming damage is first absorbed by the shield.
     * Any damage exceeding the shield's remaining value is applied directly to the target's health.
     * If the shield is a fire shield (indicated by a negative shield value in {@link BattleState}),
     * 10% of the absorbed damage is reflected back to the attacker.
     *
     * @param attacker the {@link HeroSnapshot} of the attacking hero, used for fire shield reflection;
     *                 may be {@code null} to skip reflection
     * @param target   the {@link BattleUnit} receiving the damage
     * @param raw      the raw damage amount to apply before shield absorption
     * @param state    the current {@link BattleState}, used for shield tracking
     */
    public static void applyDamage(
            HeroSnapshot attacker, BattleUnit target, int raw, BattleState state) {

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

    /**
     * Calculates the net damage dealt by an attacker to a defender.
     *
     * <p>Damage is computed as the attacker's attack stat minus the defender's defense stat,
     * with a minimum of {@code 0}.
     *
     * @param attacker the {@link BattleUnit} dealing the damage
     * @param defender the {@link BattleUnit} receiving the damage
     * @return the non-negative net damage value
     */
    public static int calculateDamage(BattleUnit attacker, BattleUnit defender) {
        return Math.max(0, attacker.getHero().getAttack() - defender.getHero().getDefense());
    }
}