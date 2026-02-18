package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Party Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository partyRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new Party for User to use in a newly created Campaign.
     */
    public Party createPartyForUser(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Party party = Party.builder()
                .owner(owner)
                .build();

        owner.getParties().add(party);

        return partyRepository.save(party);
    }

    /**
     * Returns the active party for a given campaign.
     */
    public Party getActiveParty(Long campaignId) {
        return partyRepository.findActivePartyByCampaignId(campaignId)
                .orElseThrow(() -> new RuntimeException("Active party not found for campaign " + campaignId));
    }

    /**
     * Revives and heals the party for a given campaign.
     */
    public Party reviveAndHealParty(Long campaignId) {
        Party party = getActiveParty(campaignId);
        return partyRepository.save(party);
    }

    /**
     * Recruit a hero into the party for a given campaign.
     */
    public Party recruitHero(Long campaignId, Long heroId) {
        Party party = getActiveParty(campaignId);
        return partyRepository.save(party);
    }
}
