package com.university.project.legendsofswordandwand.service.campaign.impl;

import com.university.project.legendsofswordandwand.model.*;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.ItemRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Campaign Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
class CampaignServiceImpl implements ICampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final IHeroService heroService;
    private final IPartyService partyService;
    private final ItemRepository itemRepository;
    private final HeroRepository heroRepository;

    /**
     * Returns {@code true} if the given user currently has an active campaign.
     *
     * @param userId the ID of the user to check
     * @return {@code true} if an active campaign exists for the user
     */
    @Override
    public boolean hasActiveCampaign(Long userId) {
        return campaignRepository.existsActiveCampaignByOwnerId(userId);
    }

    /**
     * Retrieves the active campaign for the given username.
     *
     * @param username the username of the player
     * @return the active {@link Campaign}
     * @throws RuntimeException if no active campaign is found
     */
    @Override
    public Campaign getActiveCampaign(String username) {
        return campaignRepository
                .findActiveCampaignByUsername(username)
                .orElseThrow(() -> new RuntimeException("No active campaign found"));
    }

    /**
     * Exits the active campaign without deactivating it, persisting any pending state changes.
     *
     * @param username the username of the player
     * @return the saved {@link Campaign}
     */
    @Override
    public Campaign exitCampaign(String username) {
        Campaign campaign = getActiveCampaign(username);
        return campaignRepository.save(campaign);
    }

    /**
     * Starts a new campaign for the given user, creating a party and a starting hero.
     *
     * @param username  the username of the player
     * @param heroName  the name of the starting hero
     * @param heroClass the {@link HeroClass} of the starting hero
     * @return the newly created and persisted {@link Campaign}
     * @throws RuntimeException if the user is not found or already has an active campaign
     */
    @Override
    public Campaign startNewCampaign(String username, String heroName, HeroClass heroClass) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (hasActiveCampaign(user.getId())) throw new RuntimeException("Campaign already in progress");

        Party party = partyService.createPartyForUser(user.getId());
        heroService.createBaseHeroForParty(party.getId(), heroName, heroClass);

        Campaign campaign =
                Campaign.builder().owner(user).active(true).currentRoom(0).party(party).build();

        return campaignRepository.save(campaign);
    }

    /**
     * Saves the party from the given campaign as a permanent saved party for the user.
     *
     * <p>All permanent heroes are restored to full HP and mana before saving. The campaign
     * is then deactivated.
     *
     * @param campaignId the ID of the campaign whose party to save
     * @param userId     the ID of the user saving the party
     * @throws RuntimeException if the user or campaign is not found, or if the user already
     *                          has 5 saved parties
     */
    @Override
    public void savePartyFromCampaign(Long campaignId, Long userId) {

        User user =
                userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        long savedCount = user.getParties().stream().filter(Party::isSaved).count();
        if (savedCount >= 5)
            throw new RuntimeException("Already have 5 saved parties - replace one first");

        Campaign campaign =
                campaignRepository
                        .findById(campaignId)
                        .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaign.getParty().getHeroes().stream()
                .filter(h -> !h.isTemporary())
                .forEach(
                        h -> {
                            h.setHealth(h.getMaxHealth());
                            h.setMana(h.getMaxMana());
                        });

        campaign.getParty().setSaved(true);
        campaign.setActive(false);
        campaignRepository.save(campaign);
    }

    /**
     * Replaces an existing saved party with the party from the given campaign.
     *
     * <p>The specified saved party is deleted before saving the new one via
     * {@link #savePartyFromCampaign}.
     *
     * @param campaignId       the ID of the campaign whose party to save
     * @param userId           the ID of the user
     * @param partyIdToReplace the ID of the saved party to replace
     * @throws RuntimeException if the user or saved party is not found
     */
    @Override
    public void replacePartyFromCampaign(Long campaignId, Long userId, Long partyIdToReplace) {

        User user =
                userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Party toReplace =
                user.getParties().stream()
                        .filter(p -> p.getId().equals(partyIdToReplace) && p.isSaved())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Saved party not found"));

        user.getParties().remove(toReplace);
        partyService.deleteParty(toReplace.getId());
        userRepository.save(user);

        savePartyFromCampaign(campaignId, userId);
    }

    /**
     * Returns the cumulative level of all heroes in the player's active campaign party.
     *
     * @param username the username of the player
     * @return the sum of all hero levels in the party
     */
    @Override
    public int getPartyCumulativeLevel(String username) {
        Campaign campaign = getActiveCampaign(username);
        return campaign.getParty().getCumulativeLevel();
    }

    /**
     * Abandons the active campaign, deleting all temporary heroes and deactivating the campaign.
     *
     * @param username the username of the player
     */
    @Override
    public void abandonCampaign(String username) {
        Campaign campaign = getActiveCampaign(username);
        campaign.getParty().getHeroes().stream()
                .filter(Hero::isTemporary)
                .toList()
                .forEach(h -> heroRepository.deleteById(h.getId()));
        campaign.setActive(false);
        campaignRepository.save(campaign);
    }

    /**
     * Completes the active campaign, calculating and persisting the final score.
     *
     * <p>The score is the sum of the party's base score and a bonus from any remaining
     * inventory items (each worth half their purchase cost multiplied by 10). Temporary
     * heroes are deleted and the campaign is deactivated.
     *
     * @param username the username of the player
     * @return the completed and saved {@link Campaign}
     */
    @Override
    public Campaign completeCampaign(String username) {

        Campaign campaign = getActiveCampaign(username);

        int baseScore = campaign.getParty().calculateScore();

        int itemScore = 0;
        Inventory inventory = campaign.getParty().getInventory();
        if (inventory != null) {
            itemScore =
                    inventory.getItemIds().stream()
                            .mapToInt(
                                    id ->
                                            itemRepository.findById(id).map(item -> (item.getCost() / 2) * 10).orElse(0))
                            .sum();
        }

        campaign.getParty().getHeroes().stream()
                .filter(Hero::isTemporary)
                .toList()
                .forEach(h -> heroRepository.deleteById(h.getId()));

        campaign.setScore(baseScore + itemScore);
        campaign.setActive(false);
        return campaignRepository.save(campaign);
    }

    /**
     * Retrieves the most recently completed campaign for the given user.
     *
     * @param username the username of the player
     * @return the most recent completed {@link Campaign}
     * @throws RuntimeException if no completed campaign is found
     */
    @Override
    public Campaign getMostRecentCompletedCampaign(String username) {
        return campaignRepository.findCompletedByOwnerUsername(username).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No completed campaign found"));
    }
}