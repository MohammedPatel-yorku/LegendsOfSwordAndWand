package com.university.project.legendsofswordandwand.dto.response;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import java.util.List;

public record CompleteCampaignInfo(
    Long campaignId,
    int score,
    int gold,
    List<Hero> heroes,
    boolean partyFull,
    List<Party> savedParties) {}
