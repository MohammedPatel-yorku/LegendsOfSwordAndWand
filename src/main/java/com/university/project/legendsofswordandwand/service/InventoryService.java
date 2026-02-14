package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** InventoryService handles business logic for Inventory objects. */
@Service
@RequiredArgsConstructor
public class InventoryService {

  private final InventoryRepository inventoryRepository;

  /**
   * Get all items in the inventory for a specific party.
   *
   * @param partyId ID of the party
   * @return List of Inventory objects
   */
  public List<Inventory> getInventoryByParty(Long partyId) {
    return inventoryRepository.findByPartyId(partyId);
  }

  /**
   * Stub for purchasing an item. Will be implemented later.
   *
   * @param partyId ID of the party
   * @param itemId ID of the item to purchase
   * @return true if purchase is successful
   */
  public boolean purchaseItem(Long partyId, Long itemId) {
    return true;
  }

  /**
   * Save or update an inventory in the database.
   *
   * @param inventory Inventory object to save
   * @return saved Inventory object
   */
  public Inventory saveInventory(Inventory inventory) {
    return inventoryRepository.save(inventory);
  }
}
