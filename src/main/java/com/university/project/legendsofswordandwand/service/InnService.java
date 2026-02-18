package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.service.InventoryService;
import com.university.project.legendsofswordandwand.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InnService {

    private final PartyService partyService;
    private final InventoryService inventoryService;

    /**
     * Load the inn view: revives and heals the party for this campaign.
     *
     * @param campaignId ID of the campaign entering the inn
     * @return Updated party state message
     */
    public String loadInnView(Long campaignId) {
        partyService.reviveAndHealParty(campaignId);
        return "Party status displayed.";
    }

    /**
     * Purchase an item in the inn for the campaign's party.
     *
     * @param campaignId ID of the campaign
     * @param itemId     ID of the item to purchase
     * @return true if purchase is successful
     */
    public boolean purchaseItem(Long campaignId, Long itemId) {
        return inventoryService.purchaseItem(campaignId, itemId);
    }

    /**
     * Recruit a hero in the inn for the campaign's party.
     *
     * @param campaignId ID of the campaign
     * @param heroId     ID of the hero to recruit
     * @return true if recruitment is successful
     */
    public boolean recruitHero(Long campaignId, Long heroId) {
        partyService.recruitHero(campaignId, heroId);
        return true;
    }

    /**
     * Exit the inn and save party state.
     *
     * @param campaignId ID of the campaign
     * @return message for next room
     */
    public String exitInn(Long campaignId) {
        return "Proceed to next room.";
    }
}
