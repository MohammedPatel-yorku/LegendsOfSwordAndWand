package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Item;
import java.util.List;

/**
 * Service interface defining the contract for inn interactions, including party healing, the item
 * shop, and hero recruitment.
 */
public interface IInnService {

  /**
   * Revives and fully heals all heroes in the campaign party and returns a healing summary.
   *
   * @param campaignId the ID of the campaign whose party to heal
   * @return a list of log strings describing the HP/mana restored to each hero
   */
  List<String> loadInnView(Long campaignId);

  /**
   * Returns all purchasable shop items, seeding the database with defaults if none exist.
   *
   * @return the list of available {@link Item}s
   */
  List<Item> getShopItems();

  /**
   * Generates and persists a list of temporary hero recruits available for this inn visit. Recruits
   * are only generated within the first 10 rooms and when the party has fewer than 5 permanent
   * heroes. Any leftover temporary recruits from a previous visit are cleaned up first.
   *
   * @param campaignId the ID of the campaign to generate recruits for
   * @return the list of generated temporary {@link Hero} recruits, or an empty list if unavailable
   */
  List<Hero> getAvailableRecruits(Long campaignId);

  /**
   * Purchases an item from the inn shop and adds it to the party's inventory.
   *
   * @param campaignId the ID of the campaign whose party is making the purchase
   * @param itemId the ID of the {@link Item} to purchase
   * @return {@code true} if the purchase succeeded; {@code false} if the party has insufficient
   *     gold
   */
  boolean purchaseItem(Long campaignId, Long itemId);

  /**
   * Recruits a temporary hero into the party as a permanent member, deducting the recruitment cost.
   *
   * @param campaignId the ID of the campaign whose party is recruiting
   * @param heroId the ID of the temporary {@link Hero} to recruit
   * @return {@code true} if recruitment succeeded; {@code false} if ineligible (full party, no
   *     gold)
   */
  boolean recruitHero(Long campaignId, Long heroId);

  /**
   * Removes all temporary (unchosen recruit) heroes from the campaign party.
   *
   * @param campaignId the ID of the campaign whose temporary recruits should be removed
   */
  void cleanupTemporaryRecruits(Long campaignId);
}
