package com.university.project.legendsofswordandwand.service.inventory.impl;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.model.Item;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import com.university.project.legendsofswordandwand.repository.ItemRepository;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.inventory.IInventoryService;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** InventoryService handles business logic for Inventory objects. */
@Service
@RequiredArgsConstructor
class InventoryServiceImpl implements IInventoryService {

  private final InventoryRepository inventoryRepository;
  private final IPartyManagementService partyManagementService;
  private final IHeroService heroService;
  private final ItemRepository itemRepository;

  /**
   * Save or update an inventory in the database.
   *
   * @param inventory Inventory object to save
   * @return saved Inventory object
   */
  @Override
  public Inventory saveInventory(Inventory inventory) {
    return inventoryRepository.save(inventory);
  }

  /**
   * Returns all {@link Item}s currently held in the active party's inventory for the given
   * campaign.
   *
   * <p>Item IDs that no longer resolve to a valid item are silently filtered out. Returns an empty
   * list if the party has no inventory.
   *
   * @param campaignId the ID of the campaign whose party inventory to retrieve
   * @return the list of {@link Item}s in the party's inventory
   */
  @Override
  public List<Item> getPartyInventoryItems(Long campaignId) {
    Party party = partyManagementService.getActiveParty(campaignId);
    Inventory inventory = party.getInventory();
    if (inventory == null) return Collections.emptyList();
    return inventory.getItemIds().stream()
        .map(id -> itemRepository.findById(id).orElse(null))
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Uses a consumable item from the party inventory on the specified hero.
   *
   * <p>If the item is a revival item, the hero is restored to full HP and mana regardless of their
   * current state. Otherwise, the hero must be alive, and their HP and mana are restored by the
   * item's respective values, capped at their maximums. One instance of the item is removed from
   * the inventory after use.
   *
   * @param campaignId the ID of the campaign whose party inventory to use from
   * @param heroId the ID of the hero to apply the item to
   * @param itemId the ID of the item to use
   * @return {@code true} on success
   * @throws RuntimeException if the item is not in the inventory, the hero is not found, the item
   *     is not found, or the hero is dead and the item does not revive
   */
  @Override
  public boolean useItem(Long campaignId, Long heroId, Long itemId) {
    Party party = partyManagementService.getActiveParty(campaignId);
    Inventory inventory = party.getInventory();
    if (inventory == null || !inventory.getItemIds().contains(itemId))
      throw new RuntimeException("Item not in inventory");

    Hero hero =
        heroService.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));
    Item item =
        itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

    if (item.isRevives()) {
      hero.setHealth(hero.getMaxHealth());
      hero.setMana(hero.getMaxMana());
    } else {
      if (hero.getHealth() <= 0)
        throw new RuntimeException(
            hero.getName() + " is dead and cannot use items. Use an Elixir to revive them first.");
      hero.setHealth(Math.min(hero.getMaxHealth(), hero.getHealth() + item.getHpRestore()));
      hero.setMana(Math.min(hero.getMaxMana(), hero.getMana() + item.getManaRestore()));
    }
    heroService.save(hero);

    inventory.getItemIds().remove(itemId); // remove one instance
    inventoryRepository.save(inventory);
    return true;
  }
}
