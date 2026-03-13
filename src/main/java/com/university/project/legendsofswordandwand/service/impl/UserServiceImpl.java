package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.ICampaignService;
import com.university.project.legendsofswordandwand.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * service layer for handling logic related to users including registration and login use case: User
 * Registration and Login
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

  private final UserRepository userRepository;
  private final ICampaignService campaignService;

  @Override
  public DashboardInfo getDashboardInfo(String username) {

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    boolean hasParty = user.getParties().stream().anyMatch(Party::isSaved);
    boolean hasCampaign = campaignService.hasActiveCampaign(user.getId());

    int partySize = 0;
    int cumulativeLevel = 0;
    int gold = 0;

    if (hasParty) {

      Party latest = user.getParties().get(user.getParties().size() - 1);
      partySize = latest.getHeroes().size();
      cumulativeLevel = latest.getCumulativeLevel();
      gold = latest.getGold();
    }

    return new DashboardInfo(username, hasParty, hasCampaign, partySize, cumulativeLevel, gold);
  }
}
