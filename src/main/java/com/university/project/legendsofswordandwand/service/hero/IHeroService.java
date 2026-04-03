package com.university.project.legendsofswordandwand.service.hero;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.Optional;

/**
 * Service interface defining the contract for hero creation, levelling, experience management, and
 * persistence.
 */
public interface IHeroService {

  /**
   * Creates a new Level 1 hero with base stats and adds it to the given party.
   *
   * @param partyId the ID of the party to add the hero to
   * @param heroName the display name of the new hero
   * @param heroClass the starting {@link HeroClass} of the hero
   * @throws RuntimeException if the party is not found
   */
  void createBaseHeroForParty(Long partyId, String heroName, HeroClass heroClass);

  /**
   * Creates a hero at an arbitrary level for the given party, applying all stat gains, class
   * bonuses, and XP thresholds that the hero would have accumulated by reaching that level through
   * normal play.
   *
   * <p>This is the single authoritative place that knows how to initialise a hero to a target
   * level. It exists so callers (e.g. the inn recruit system) never need to inline the sequence of
   * class-level-counter assignment, repeated {@code applyLevelUp} calls, class bonus application,
   * and XP threshold back-calculation.
   *
   * @param party the {@link Party} the hero belongs to
   * @param heroName the display name of the new hero
   * @param heroClass the {@link HeroClass} to level up in
   * @param level the target level (1–20); 1 produces a standard base-stat hero
   * @return the unsaved {@link Hero} entity, ready to be marked temporary and persisted by the
   *     caller (so the caller retains control over persistence timing)
   * @throws IllegalArgumentException if {@code level} is less than 1 or greater than 20
   */
  Hero createHeroAtLevel(Party party, String heroName, HeroClass heroClass, int level);

  /**
   * Applies a level-up to the hero using the chosen class, updating stats and potentially
   * triggering a hybrid class promotion if applicable.
   *
   * @param heroId the ID of the hero to level up
   * @param chosenClass the {@link HeroClass} the player selected for this level-up
   * @return the updated and saved {@link Hero}
   * @throws RuntimeException if the hero is not found, is at max level, or has insufficient XP
   */
  Hero levelUp(Long heroId, HeroClass chosenClass);

  /**
   * Adds the given amount of experience to the hero and persists the change.
   *
   * @param heroId the ID of the hero
   * @param amount the amount of XP to add
   * @return the updated and saved {@link Hero}
   * @throws RuntimeException if the hero is not found
   */
  Hero addExperience(Long heroId, int amount);

  /**
   * Returns {@code true} if the hero has enough experience to level up and has not yet reached the
   * maximum level of 20.
   *
   * @param heroId the ID of the hero to check
   * @return {@code true} if a level-up is pending
   * @throws RuntimeException if the hero is not found
   */
  boolean isLevelUpPending(Long heroId);

  /**
   * Finds a hero by their database ID.
   *
   * @param heroId the ID of the hero
   * @return an {@link Optional} containing the hero if found, or empty otherwise
   */
  Optional<Hero> findById(Long heroId);

  /**
   * Persists the given hero entity to the database.
   *
   * @param hero the {@link Hero} to save
   * @return the saved {@link Hero}
   */
  Hero save(Hero hero);

  /**
   * Permanently deletes the hero with the given ID from the database.
   *
   * @param heroId the ID of the hero to delete
   */
  void delete(Long heroId);
}
