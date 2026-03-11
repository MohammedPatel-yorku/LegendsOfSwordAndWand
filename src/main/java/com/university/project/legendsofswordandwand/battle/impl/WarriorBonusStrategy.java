package com.university.project.legendsofswordandwand.battle.impl;

import com.university.project.legendsofswordandwand.battle.ClassBonusStrategy;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;

public class WarriorBonusStrategy implements ClassBonusStrategy {
    @Override
    public HeroClass getHeroClass() {

        return HeroClass.WARRIOR;
    }

    @Override
    public void applyLevelBonus(Hero hero) {

        hero.setAttack(hero.getAttack() + 2);
        hero.setDefense(hero.getDefense() + 3);
    }
}
