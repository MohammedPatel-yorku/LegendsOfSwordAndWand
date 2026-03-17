package com.university.project.legendsofswordandwand.service.battle.impl;

import com.university.project.legendsofswordandwand.battle.DamageCalculator;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class BattleServiceImpl implements IBattleService {

  private final DamageCalculator damageCalculator;

  @Override
  public void executeAttack(Hero attacker, Hero defender) {
    int damage = damageCalculator.calculateDamage(attacker.getAttack(), defender.getDefense());
    defender.setHealth(Math.max(0, defender.getHealth() - damage));
  }

  @Override
  public void executeDefend(Hero unit) {
    unit.setHealth(Math.min(unit.getMaxHealth(), unit.getHealth() + 10));
    unit.setMana(Math.min(unit.getMaxMana(), unit.getMana() + 5));
  }
}
