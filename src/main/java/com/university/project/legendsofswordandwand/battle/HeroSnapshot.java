package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * A serializable snapshot of a {@link Hero}'s stats and state at the start of a battle.
 *
 * <p>Decouples battle logic from the persistent {@link Hero} entity, allowing stats to be mutated
 * freely during combat without affecting the underlying model until results are explicitly written
 * back. Implements {@link Serializable} to support session persistence.
 */
@Getter
@Setter
public class HeroSnapshot implements Serializable {

  /** The hero's database ID. {@code null} for enemy units. */
  private final Long id;

  /** The hero's display name. */
  private final String name;

  /** The hero's starting class, used as a fallback when no primary class is set. */
  private final HeroClass startingClass;

  /** The hero's promoted primary class, or {@code null} if not yet promoted. */
  private final HeroClass primaryClass;

  /** The hero's hybrid class, determining ability specialisation. */
  private final HybridClass hybridClass;

  /** The hero's current level. */
  private int level;

  /** The hero's current health. */
  private int health;

  /** The hero's maximum health. */
  private int maxHealth;

  /** The hero's attack stat. */
  private int attack;

  /** The hero's defense stat. */
  private int defense;

  /** The hero's current mana. */
  private int mana;

  /** The hero's maximum mana. */
  private int maxMana;

  /** The hero's current experience points. */
  private int experience;

  /** The experience points required to reach the next level. */
  private int experienceToNextLevel;

  /**
   * Constructs a {@code HeroSnapshot} by copying all relevant fields from the given {@link Hero}.
   *
   * @param hero the {@link Hero} entity to snapshot
   */
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

  /**
   * Returns {@code true} if this hero is still alive.
   *
   * @return {@code true} if current health is greater than {@code 0}
   */
  public boolean isAlive() {
    return health > 0;
  }
}
