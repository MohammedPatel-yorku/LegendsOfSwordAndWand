package com.university.project.legendsofswordandwand.battle;

import org.springframework.stereotype.Component;

@Component
public class DamageCalculator {
    public int calculateDamage(int attackerAttack, int defenderDefense) {
        int damage = attackerAttack - defenderDefense;
        return Math.max(0, damage);
    }
}