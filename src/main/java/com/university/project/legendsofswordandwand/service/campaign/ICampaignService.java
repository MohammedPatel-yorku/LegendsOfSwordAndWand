package com.university.project.legendsofswordandwand.service.campaign;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;

public interface ICampaignService {

  Campaign startNewCampaign(String username, String heroName, HeroClass heroClass);

  boolean hasActiveCampaign(Long userId);

  Campaign getActiveCampaign(String username);

  Campaign exitCampaign(String username);

  Campaign completeCampaign(String username);

  Campaign getMostRecentCompletedCampaign(String username);

  void savePartyFromCampaign(Long campaignId, Long userId);

  void replacePartyFromCampaign(Long campaignId, Long userId, Long partyIdToReplace);

  int getPartyCumulativeLevel(String username);
}
