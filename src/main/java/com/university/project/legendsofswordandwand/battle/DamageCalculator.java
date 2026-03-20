package com.university.project.legendsofswordandwand.battle;

import org.springframework.stereotype.Component;

/**
 * Spring component responsible for calculating net damage between two combatants.
 */
@Component
public class DamageCalculator {

    /**
     * Calculates the net damage dealt by an attacker to a defender.
     *
     * <p>Damage is computed as the attacker's attack stat minus the defender's defense stat,
     * with a minimum of {@code 0}.
     *
     * @param attackerAttack  the attack stat of the attacking unit
     * @param defenderDefense the defense stat of the defending unit
     * @return the non-negative net damage value
     */
    public int calculateDamage(int attackerAttack, int defenderDefense) {
        int damage = attackerAttack - defenderDefense;
        return Math.max(0, damage);
    }
}