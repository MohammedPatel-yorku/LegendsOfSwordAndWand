package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.battle.ClassBonusStrategy;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

@Component
public class ChaosBonusStrategy implements ClassBonusStrategy {
  @Override
  public HeroClass getHeroClass() {

    return HeroClass.CHAOS;
  }

  @Override
  public void applyLevelBonus(Hero hero) {

    hero.setAttack(hero.getAttack() + 3);
    hero.setHealth(hero.getHealth() + 5);
    hero.setMaxHealth(hero.getMaxHealth() + 5);
  }
}
