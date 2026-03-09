package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Inventory;
import java.util.List;

public interface IInventoryService {

  List<Inventory> getInventoryByParty(Long partyId);

  boolean purchaseItem(Long campaignId, Long itemId);

  Inventory saveInventory(Inventory inventory);
}
