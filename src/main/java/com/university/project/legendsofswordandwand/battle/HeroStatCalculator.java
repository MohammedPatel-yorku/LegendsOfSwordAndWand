package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeroStatCalculator {

  private final List<ClassBonusStrategy> strategies;

  public void applyLevelUp(Hero hero, HeroClass heroClass) {

    applyBaseGain(hero);

    if (hero.isHybrid()) {
      applyClassBonus(hero, hero.getPrimaryClass());
      applyClassBonus(hero, hero.getSecondaryClass());
    } else if (hero.getPrimaryClass() != null && hero.getPrimaryClass() == heroClass) {
      applyClassBonus(hero, heroClass);
      applyClassBonus(hero, heroClass);
    } else {
      applyClassBonus(hero, heroClass);
    }

    hero.setExperienceToNextLevel(calculateExpThreshold(hero.getLevel() + 1));
  }

  public HybridClass resolveHybridClass(HeroClass primary, HeroClass secondary) {

    HeroClass a = primary.ordinal() <= secondary.ordinal() ? primary : secondary;
    HeroClass b = primary.ordinal() <= secondary.ordinal() ? secondary : primary;

    if (a == HeroClass.ORDER && b == HeroClass.CHAOS) return HybridClass.HERETIC;
    if (a == HeroClass.ORDER && b == HeroClass.WARRIOR) return HybridClass.PALADIN;
    if (a == HeroClass.ORDER && b == HeroClass.MAGE) return HybridClass.PROPHET;
    if (a == HeroClass.CHAOS && b == HeroClass.WARRIOR) return HybridClass.ROGUE;
    if (a == HeroClass.CHAOS && b == HeroClass.MAGE) return HybridClass.SORCERER;
    if (a == HeroClass.WARRIOR && b == HeroClass.MAGE) return HybridClass.WARLOCK;

    throw new IllegalArgumentException("Cannot hybridize the same class: " + primary);
  }

  private int calculateExpThreshold(int level) {

    if (level <= 1) return 0;
    return calculateExpThreshold(level - 1) + 500 + 75 * level + 20 * level * level;
  }

  private void applyBaseGain(Hero hero) {

    hero.setLevel(hero.getLevel() + 1);
    hero.setAttack(hero.getAttack() + 1);
    hero.setDefense(hero.getDefense() + 1);
    hero.setHealth(hero.getHealth() + 5);
    hero.setMaxHealth(hero.getMaxHealth() + 5);
    hero.setMana(hero.getMana() + 2);
    hero.setMaxMana(hero.getMaxMana() + 2);
  }

  private void applyClassBonus(Hero hero, HeroClass heroClass) {

    getStrategy(heroClass).applyLevelBonus(hero);
  }

  private ClassBonusStrategy getStrategy(HeroClass heroClass) {

    return strategies.stream()
        .filter(s -> s.getHeroClass() == heroClass)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No strategy found for " + heroClass));
  }
}
