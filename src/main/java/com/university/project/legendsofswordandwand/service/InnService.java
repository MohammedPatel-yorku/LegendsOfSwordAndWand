package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** InventoryService handles business logic for Inventory objects. */
@Service
@RequiredArgsConstructor
public class InnService {

  private final InventoryRepository inventoryRepository;
  private final PartyRepository partyRepository;

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
   * Purchase an item for the party belonging to the given campaign.
   *
   * @param campaignId ID of the campaign
   * @param itemId ID of the item to purchase
   * @return true if purchase is successful
   */
  public boolean purchaseItem(Long campaignId, Long itemId) {

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

  /**
   * Load the inn view: revives and heals party.
   *
   * @param partyId ID of the party entering the inn
   * @return Updated party state
   */
  public String loadInnView(Long partyId) {
    reviveAndHealParty(partyId);
    return "Party status displayed.";
  }

  /** Revive and heal all heroes in the party */
  public void reviveAndHealParty(Long partyId) {
    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found."));
  }

  /** Recruit a hero in the inn */
  public boolean recruitHero(Long partyId, Long heroId) {
    return true;
  }

  /** Exit the inn and save party state */
  public String exitInn(Long partyId) {
    return "Proceed to next room.";
  }
}
