package com.university.project.legendsofswordandwand.service.user.impl;

import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * service layer for handling logic related to users including registration and login use case: User
 * Registration and Login
 */
@Service
@RequiredArgsConstructor
class UserServiceImpl implements IUserService {

  private final UserRepository userRepository;
  private final CampaignRepository campaignRepository;

  @Override
  public DashboardInfo getDashboardInfo(String username) {

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    boolean hasParty = user.getParties().stream().anyMatch(Party::isSaved);
    boolean hasCampaign = campaignRepository.existsActiveCampaignByOwnerId(user.getId());

    int partySize = 0;
    int cumulativeLevel = 0;
    int gold = 0;

    if (hasParty) {

      Party latest = user.getParties().get(user.getParties().size() - 1);
      partySize = (int) latest.getHeroes().stream()
              .filter(h -> !h.isTemporary())
              .count();
      cumulativeLevel = latest.getCumulativeLevel();
      gold = latest.getGold();
    }

    return new DashboardInfo(username, hasParty, hasCampaign, partySize, cumulativeLevel, gold);
  }

  @Override
  public Long getUserIdByUsername(String username) {

    return userRepository
        .findByUsername(username)
        .map(User::getId)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));
  }
}
