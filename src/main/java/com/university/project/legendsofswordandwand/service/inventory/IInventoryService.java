package com.university.project.legendsofswordandwand.service.inventory;

import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.model.Item;
import java.util.List;

public interface IInventoryService {

  Inventory saveInventory(Inventory inventory);

  boolean useItem(Long campaignId, Long heroId, Long itemId);

  List<Item> getPartyInventoryItems(Long campaignId);
}
