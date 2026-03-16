package com.university.project.legendsofswordandwand.dto.response;

import com.university.project.legendsofswordandwand.model.Hero;
import java.util.List;

public record CampaignViewInfo(Long campaignId, int currentRoom, int gold, List<Hero> heroes) {}
