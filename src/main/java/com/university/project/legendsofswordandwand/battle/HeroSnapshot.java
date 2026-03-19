package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeroSnapshot implements Serializable {

  private final Long id; // null for enemies
  private final String name;
  private final HeroClass startingClass;
  private final HeroClass primaryClass;
  private final HybridClass hybridClass;

  private int level;
  private int health;
  private int maxHealth;
  private int attack;
  private int defense;
  private int mana;
  private int maxMana;
  private int experience;
  private int experienceToNextLevel;

  public HeroSnapshot(Hero hero) {
    this.id = hero.getId();
    this.name = hero.getName();
    this.startingClass = hero.getStartingClass();
    this.primaryClass = hero.getPrimaryClass();
    this.hybridClass = hero.getHybridClass();
    this.level = hero.getLevel();
    this.health = hero.getHealth();
    this.maxHealth = hero.getMaxHealth();
    this.attack = hero.getAttack();
    this.defense = hero.getDefense();
    this.mana = hero.getMana();
    this.maxMana = hero.getMaxMana();
    this.experience = hero.getExperience();
    this.experienceToNextLevel = hero.getExperienceToNextLevel();
  }

  public boolean isAlive() {
    return health > 0;
  }
}
