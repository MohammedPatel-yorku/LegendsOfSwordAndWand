package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;

public interface ClassBonusStrategy {

  HeroClass getHeroClass();

  void applyLevelBonus(Hero hero);
}
