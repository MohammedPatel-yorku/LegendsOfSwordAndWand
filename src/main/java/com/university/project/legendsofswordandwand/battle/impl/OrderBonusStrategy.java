package com.university.project.legendsofswordandwand.battle.impl;

import com.university.project.legendsofswordandwand.battle.ClassBonusStrategy;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

@Component
public class OrderBonusStrategy implements ClassBonusStrategy {

  @Override
  public HeroClass getHeroClass() {

    return HeroClass.ORDER;
  }

  @Override
  public void applyLevelBonus(Hero hero) {

    hero.setMana(hero.getMana() + 5);
    hero.setMaxMana(hero.getMaxMana() + 5);
    hero.setDefense(hero.getDefense() + 2);
  }
}
