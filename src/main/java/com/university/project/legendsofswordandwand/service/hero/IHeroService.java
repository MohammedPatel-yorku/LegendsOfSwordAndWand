package com.university.project.legendsofswordandwand.service.hero;

import com.university.project.legendsofswordandwand.model.Hero;
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
