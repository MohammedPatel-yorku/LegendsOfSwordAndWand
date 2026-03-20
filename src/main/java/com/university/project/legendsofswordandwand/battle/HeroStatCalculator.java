package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeroStatCalculator {

  private final List<ClassBonusStrategy> strategies;
  @Getter private final HybridClassResolver hybridClassResolver;

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

    hero.setExperienceToNextLevel(calculateExpThreshold(hero.getLevel()));
  }

  public void applyClassBonusOnly(Hero hero, HeroClass heroClass) {

    applyClassBonus(hero, heroClass);
  }

  private int calculateExpThreshold(int level) {
    if (level <= 1) return 500 + 75 + 20; // Exp(1) = 0 + 500 + 75*1 + 20*1 = 595
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
