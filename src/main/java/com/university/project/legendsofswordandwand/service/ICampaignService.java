package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import java.util.List;

public interface ICampaignService {

  Campaign startNewCampaign(String username, String heroName, HeroClass heroClass);

  boolean hasActiveCampaign(Long userId);

  Campaign getActiveCampaign(String username);

  RoomType enterNextRoom(String username);

  Campaign exitCampaign(String username);

  int calculateScore(Campaign campaign);

  List<ProfileInfo.CampaignResultInfo> getCampaignResultsForUser(Long userId);

  void savePartyFromCampaign(Long campaignId, Long userId);

  void replacePartyFromCampaign(Long campaignId, Long userId, Long partyIdToReplace);

  Campaign completeCampaign(String username);

  boolean isCampaignComplete(String username);

  Campaign getMostRecentCompletedCampaign(String username);

  CompleteCampaignInfo getCompletionData(String username);

  CampaignViewInfo getCampaignViewData(String username);
}
