package com.university.project.legendsofswordandwand.dto;

public record DashboardInfo(
    String username,
    boolean hasParty,
    boolean hasCampaign,
    int partySize,
    int cumulativeLevel,
    int gold) {}
