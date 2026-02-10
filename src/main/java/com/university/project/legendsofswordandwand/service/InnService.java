package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InnService {

    private final PartyRepository partyRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Load the inn view: revives and heals party (stub).
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
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found."));
    }

    /** Purchase an item in the inn */
    public boolean purchaseItem(Long itemId) {
        return true;
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
