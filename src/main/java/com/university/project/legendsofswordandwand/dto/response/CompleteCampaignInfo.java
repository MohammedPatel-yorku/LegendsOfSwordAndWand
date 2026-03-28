package com.university.project.legendsofswordandwand.dto.response;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import java.util.List;

/**
 * Immutable view DTO carrying the data needed to render the campaign completion page.
 *
 * @param campaignId the ID of the completed campaign
 * @param score the final calculated score for this run
 * @param gold the gold remaining in the party at the end of the campaign
 * @param heroes the surviving heroes at campaign end
 * @param partyFull {@code true} if the player already has 5 saved parties (must replace one)
 * @param savedParties the player's currently saved parties, for the replace-party selection UI
 */
public record CompleteCampaignInfo(
    Long campaignId,
    int score,
    int gold,
    List<Hero> heroes,
    boolean partyFull,
    List<Party> savedParties) {}
