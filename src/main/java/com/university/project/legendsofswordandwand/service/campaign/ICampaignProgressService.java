package com.university.project.legendsofswordandwand.service.campaign;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import java.util.List;

public interface ICampaignProgressService {

  RoomType enterNextRoom(String username);

  boolean isCampaignComplete(String username);

  int calculateAndPersistScore(Campaign campaign);

  List<ProfileInfo.CampaignResultInfo> getCampaignResultsForUser(Long userId);

  CompleteCampaignInfo getCompletionData(String username);

  CampaignViewInfo getCampaignViewData(String username);
}
