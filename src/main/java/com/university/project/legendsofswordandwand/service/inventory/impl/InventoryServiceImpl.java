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
      hero.setHealth(Math.min(hero.getMaxHealth(), hero.getHealth() + item.getHpRestore()));
      hero.setMana(Math.min(hero.getMaxMana(), hero.getMana() + item.getManaRestore()));
    }
    heroService.save(hero);

    inventory.getItemIds().remove(itemId); // remove one instance
    inventoryRepository.save(inventory);
    return true;
  }
}
