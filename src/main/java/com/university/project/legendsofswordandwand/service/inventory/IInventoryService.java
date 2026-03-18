package com.university.project.legendsofswordandwand.service.inventory;

import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.model.Item;

import java.util.List;

public interface IInventoryService {

  List<Inventory> getInventoryByParty(Long partyId);

  boolean purchaseItem(Long campaignId, Long itemId);

  Inventory saveInventory(Inventory inventory);

  boolean useItem(Long campaignId, Long heroId, Long itemId);

  List<Item> getPartyInventoryItems(Long campaignId);
}
