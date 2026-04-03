package com.university.project.legendsofswordandwand.service.hero.impl;

import com.university.project.legendsofswordandwand.battle.HeroStatCalculator;
import com.university.project.legendsofswordandwand.battle.HybridClassResolver;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Hero Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
class HeroServiceImpl implements IHeroService {

  private final HeroRepository heroRepository;
  private final PartyRepository partyRepository;
  private final HeroStatCalculator heroStatCalculator;
  private final HybridClassResolver hybridClassResolver;

  /**
   * Creates a new base stat (Level 1, 100 HP, 10 Attack) Hero for requesting Party.
   *
   * @param partyId ID of Party to create Hero Object for
   * @param selectedHeroName name to assign to the hero
   * @param selectedHeroClass hero class to assign to the hero
   * @throws RuntimeException if the party is not found
   */
  @Override
  public void createBaseHeroForParty(
      Long partyId, String selectedHeroName, HeroClass selectedHeroClass) {
    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party Not Found"));

    Hero hero =
        Hero.builder().name(selectedHeroName).startingClass(selectedHeroClass).party(party).build();

    incrementClassLevel(hero, selectedHeroClass);
    heroStatCalculator.applyClassBonusOnly(hero, selectedHeroClass);

    party.getHeroes().add(hero);
    heroRepository.save(hero);
  }

  /**
   * Creates a hero at the given target level for the provided party, applying all stat gains, class
   * bonuses, and XP thresholds that the hero would have accumulated through normal play.
   *
   * <p>This is the single authoritative implementation of "initialise a hero to level N". It
   * replaces the inline data clump that previously appeared in {@code InnServiceImpl
   * .getAvailableRecruits()}: class-level-counter assignment, repeated {@code applyLevelUp} calls,
   * class bonus application, and XP back-calculation are all performed here.
   *
   * <p>The returned hero is <em>not</em> persisted — the caller is responsible for saving it (and,
   * for inn recruits, marking it temporary before doing so).
   *
   * @param party the {@link Party} the hero belongs to
   * @param heroName the hero's display name
   * @param heroClass the {@link HeroClass} to level up in
   * @param level the target level (1–20)
   * @return the initialised, unsaved {@link Hero}
   * @throws IllegalArgumentException if {@code level} is outside the range 1–20
   */
  @Override
  public Hero createHeroAtLevel(Party party, String heroName, HeroClass heroClass, int level) {
    if (level < 1 || level > 20) {
      throw new IllegalArgumentException("Hero level must be between 1 and 20, got: " + level);
    }

    Hero hero = Hero.builder().name(heroName).startingClass(heroClass).party(party).build();

    // Set the class-level counter to the target level
    setClassLevel(hero, heroClass, level);

    // Apply level-up stat gains for levels 2 through target (each call also sets
    // experienceToNextLevel)
    for (int lvl = 1; lvl < level; lvl++) {
      heroStatCalculator.applyLevelUp(hero, heroClass);
    }

    // Apply the class bonus for level 1 (the base class bonus not covered by applyLevelUp)
    heroStatCalculator.applyClassBonusOnly(hero, heroClass);

    // Explicitly fix the level field — applyLevelUp increments it, so after (level-1) calls
    // it reads (level-1). We then set it to the intended target.
    hero.setLevel(level);

    // Set the hero's starting XP to the floor of their current level so they do not
    // immediately need a level-up. Uses the public step formula from HeroStatCalculator
    // rather than inlining the magic numbers.
    if (level > 1) {
      int prevThreshold =
          hero.getExperienceToNextLevel() - heroStatCalculator.getExpStepForLevel(level);
      hero.setExperience(Math.max(0, prevThreshold));
    }

    return hero;
  }

  /**
   * Applies a level-up to the given hero using the chosen class.
   *
   * <p>For hybrid heroes, the primary class is always used regardless of {@code chosenClass}. A
   * hero becomes hybrid when they reach level 5 in a second class after already having a primary
   * class set, at which point the {@link HybridClassResolver} assigns their hybrid class.
   *
   * @param heroId the ID of the hero to level up
   * @param chosenClass the {@link HeroClass} the player selected for this level-up
   * @return the updated and saved {@link Hero}
   * @throws RuntimeException if the hero is not found, is already at max level, or has insufficient
   *     XP
   */
  @Override
  public Hero levelUp(Long heroId, HeroClass chosenClass) {

    Hero hero =
        heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero Not Found"));

    if (hero.getLevel() >= 20) throw new RuntimeException("Hero is already at max level");

    if (!isLevelUpPending(heroId))
      throw new RuntimeException("Hero does not have enough XP to level up");

    HeroClass effectiveChoice = hero.isHybrid() ? hero.getPrimaryClass() : chosenClass;

    incrementClassLevel(hero, effectiveChoice);

    if (!hero.isHybrid()
        && hero.getPrimaryClass() == null
        && getClassLevel(hero, chosenClass) == 5) {
      hero.setPrimaryClass(chosenClass);
    }

    if (!hero.isHybrid()
        && hero.getPrimaryClass() != null
        && getClassLevel(hero, chosenClass) == 5) {
      hero.setSecondaryClass(chosenClass);
      hero.setHybrid(true);
      hero.setHybridClass(hybridClassResolver.resolve(hero.getPrimaryClass(), chosenClass));
    }

    heroStatCalculator.applyLevelUp(hero, effectiveChoice);

    return heroRepository.save(hero);
  }

  /**
   * Adds experience points to the given hero and persists the change.
   *
   * @param heroId the ID of the hero to award experience to
   * @param amount the amount of experience to add
   * @return the updated and saved {@link Hero}
   * @throws RuntimeException if the hero is not found
   */
  @Override
  public Hero addExperience(Long heroId, int amount) {
    Hero hero =
        heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));
    hero.setExperience(hero.getExperience() + amount);
    return heroRepository.save(hero);
  }

  /**
   * Returns {@code true} if the hero has enough experience to level up and has not reached the
   * maximum level of 20.
   *
   * @param heroId the ID of the hero to check
   * @return {@code true} if a level-up is pending
   * @throws RuntimeException if the hero is not found
   */
  @Override
  public boolean isLevelUpPending(Long heroId) {
    Hero hero =
        heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));
    return hero.getLevel() < 20 && hero.getExperience() >= hero.getExperienceToNextLevel();
  }

  /**
   * Finds a hero by their ID.
   *
   * @param heroId the ID of the hero to find
   * @return an {@link Optional} containing the hero if found, or empty otherwise
   */
  @Override
  public Optional<Hero> findById(Long heroId) {
    return heroRepository.findById(heroId);
  }

  /**
   * Persists the given hero entity.
   *
   * @param hero the {@link Hero} to save
   * @return the saved {@link Hero}
   */
  @Override
  public Hero save(Hero hero) {
    return heroRepository.save(hero);
  }

  /**
   * Deletes the hero with the given ID.
   *
   * @param heroId the ID of the hero to delete
   */
  @Override
  public void delete(Long heroId) {
    heroRepository.deleteById(heroId);
  }

  /**
   * Sets the class-level counter for the given hero and class to the specified value directly. Used
   * by {@link #createHeroAtLevel} to stamp the target level without running the increment loop.
   *
   * @param hero the {@link Hero} to update
   * @param heroClass the {@link HeroClass} whose counter to set
   * @param level the value to set the counter to
   */
  private void setClassLevel(Hero hero, HeroClass heroClass, int level) {
    switch (heroClass) {
      case ORDER -> hero.setOrderLevels(level);
      case CHAOS -> hero.setChaosLevels(level);
      case WARRIOR -> hero.setWarriorLevels(level);
      case MAGE -> hero.setMageLevels(level);
    }
  }

  /**
   * Increments the class-specific level counter for the given hero and class by one.
   *
   * @param hero the {@link Hero} to update
   * @param heroClass the {@link HeroClass} whose level counter to increment
   */
  private void incrementClassLevel(Hero hero, HeroClass heroClass) {
    switch (heroClass) {
      case ORDER -> hero.setOrderLevels(hero.getOrderLevels() + 1);
      case CHAOS -> hero.setChaosLevels(hero.getChaosLevels() + 1);
      case WARRIOR -> hero.setWarriorLevels(hero.getWarriorLevels() + 1);
      case MAGE -> hero.setMageLevels(hero.getMageLevels() + 1);
    }
  }

  /**
   * Returns the number of levels the hero has accumulated in the given class.
   *
   * @param hero the {@link Hero} to query
   * @param heroClass the {@link HeroClass} to look up
   * @return the number of levels in the specified class
   */
  private int getClassLevel(Hero hero, HeroClass heroClass) {
    return switch (heroClass) {
      case ORDER -> hero.getOrderLevels();
      case CHAOS -> hero.getChaosLevels();
      case WARRIOR -> hero.getWarriorLevels();
      case MAGE -> hero.getMageLevels();
    };
  }
}
