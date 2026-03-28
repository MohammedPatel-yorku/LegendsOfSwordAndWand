package com.university.project.legendsofswordandwand.dto.response;

import com.university.project.legendsofswordandwand.model.Hero;
import java.util.List;

/**
 * Immutable view DTO carrying the information needed to render the main campaign page.
 *
 * @param campaignId the ID of the active campaign
 * @param currentRoom the room number the player is currently on (0–30)
 * @param gold the current gold total of the campaign party
 * @param heroes the list of permanent heroes in the party
 */
public record CampaignViewInfo(Long campaignId, int currentRoom, int gold, List<Hero> heroes) {}
