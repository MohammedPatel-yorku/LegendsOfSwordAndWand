package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.dto.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.List;

public interface ICampaignService {

  Campaign startNewCampaign(String username, String heroName, HeroClass heroClass);

  boolean hasActiveCampaign(Long userId);

  List<ProfileInfo.CampaignResultInfo> getCampaignResultsForUser(Long userId);

  void savePartyFromCampaign(Long campaignId, Long userId);

  void replacePartyFromCampaign(Long campaignId, Long userId, Long partyIdToReplace);
}
