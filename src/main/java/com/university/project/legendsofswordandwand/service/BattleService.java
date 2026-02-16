package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.battle.TurnManager;
import com.university.project.legendsofswordandwand.battle.DamageCalculator;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BattleService {
    private final TurnManager turnManager;
    private final DamageCalculator damageCalculator;
    private final PartyRepository partyRepository;

    public void executeAttack(Hero attacker, Hero defender) {
        int damage = damageCalculator.calculateDamage(attacker.getAttack(), defender.getDefense());
        defender.setHealth(Math.max(0, defender.getHealth() - damage));
    }

    public void executeDefend(Hero unit) {
        unit.setHealth(Math.min(100, unit.getHealth() + 10));
        unit.setMana(Math.min(50, unit.getMana() + 5));
    }
}