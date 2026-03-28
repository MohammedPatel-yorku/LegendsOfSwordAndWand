package com.university.project.legendsofswordandwand.service.inventory;

import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.model.Item;
import java.util.List;

/** Service interface defining the contract for party inventory management. */
public interface IInventoryService {

  /**
   * Persists the given {@link Inventory} entity to the database.
   *
   * @param inventory the inventory to save
   * @return the saved {@link Inventory}
   */
  Inventory saveInventory(Inventory inventory);

  /**
   * Uses an item from the campaign party's inventory on the specified hero, applying its HP/mana
   * effects and removing it from the inventory.
   *
   * @param campaignId the ID of the campaign whose inventory to draw from
   * @param heroId the ID of the hero to apply the item to
   * @param itemId the ID of the item to use
   * @return {@code true} if the item was successfully used; {@code false} if not found or the hero
   *     is already at full stats
   */
  boolean useItem(Long campaignId, Long heroId, Long itemId);

  /**
   * Returns all items currently held in the campaign party's inventory.
   *
   * @param campaignId the ID of the campaign
   * @return a list of {@link Item}s in the party's inventory
   */
  List<Item> getPartyInventoryItems(Long campaignId);
}
