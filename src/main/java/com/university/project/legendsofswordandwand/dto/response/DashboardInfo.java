package com.university.project.legendsofswordandwand.dto.response;

/**
 * Immutable view DTO carrying the summary data needed to render the player dashboard.
 *
 * @param username the player's username
 * @param hasParty {@code true} if the player has at least one saved party
 * @param hasCampaign {@code true} if the player has an active campaign in progress
 * @param partySize the number of heroes in the player's most recent active party
 * @param cumulativeLevel the sum of all hero levels in the active party
 * @param gold the current gold total of the active party
 */
public record DashboardInfo(
    String username,
    boolean hasParty,
    boolean hasCampaign,
    int partySize,
    int cumulativeLevel,
    int gold) {}
