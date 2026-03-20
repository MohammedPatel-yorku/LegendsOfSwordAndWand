package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.battle.ClassBonusStrategy;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

@Component
public class MageBonusStrategy implements ClassBonusStrategy {
  @Override
  public HeroClass getHeroClass() {

    return HeroClass.MAGE;
  }

  @Override
  public void applyLevelBonus(Hero hero) {

    hero.setMana(hero.getMana() + 5);
    hero.setMaxMana(hero.getMaxMana() + 5);
    hero.setAttack(hero.getAttack() + 1);
  }
}
