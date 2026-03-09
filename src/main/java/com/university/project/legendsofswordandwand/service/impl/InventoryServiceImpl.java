package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import com.university.project.legendsofswordandwand.service.IInventoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** InventoryService handles business logic for Inventory objects. */
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {

  private final InventoryRepository inventoryRepository;

  /**
   * Get all items in the inventory for a specific party.
   *
   * @param partyId ID of the party
   * @return List of Inventory objects
   */
  @Override
  public List<Inventory> getInventoryByParty(Long partyId) {
    return inventoryRepository.findByPartyId(partyId);
  }

  /**
   * Purchase an item for the party belonging to the given campaign.
   *
   * @param campaignId ID of the campaign
   * @param itemId ID of the item to purchase
   * @return true if purchase is successful
   */
  @Override
  public boolean purchaseItem(Long campaignId, Long itemId) {

    return true;
  }

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
}
