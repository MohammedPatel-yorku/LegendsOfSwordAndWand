package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.service.IInnService;
import com.university.project.legendsofswordandwand.service.IInventoryService;
import com.university.project.legendsofswordandwand.service.IPartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** InventoryService handles business logic for Inventory objects. */
@Service
@RequiredArgsConstructor
public class InnServiceImpl implements IInnService {

  private final IPartyService partyService;
  private final IInventoryService inventoryService;

  @Override
  public String loadInnView(Long campaignId) {
    partyService.reviveAndHealParty(campaignId);
    return "Party status displayed.";
  }

  @Override
  public boolean purchaseItem(Long campaignId, Long itemId) {
    return inventoryService.purchaseItem(campaignId, itemId);
  }

  @Override
  public boolean recruitHero(Long campaignId, Long heroId) {
    partyService.recruitHero(campaignId, heroId);
    return true;
  }

  @Override
  public String exitInn(Long campaignId) {
    return "Proceed to next room.";
  }
}
