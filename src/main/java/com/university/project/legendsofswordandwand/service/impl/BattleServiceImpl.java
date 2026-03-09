package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.battle.DamageCalculator;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.service.IBattleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BattleServiceImpl implements IBattleService {

  private final DamageCalculator damageCalculator;

  @Override
  public void executeAttack(Hero attacker, Hero defender) {
    int damage = damageCalculator.calculateDamage(attacker.getAttack(), defender.getDefense());
    defender.setHealth(Math.max(0, defender.getHealth() - damage));
  }

  @Override
  public void executeDefend(Hero unit) {
    unit.setHealth(unit.getHealth() + 10);
    unit.setMana(unit.getMana() + 5);
  }
}
