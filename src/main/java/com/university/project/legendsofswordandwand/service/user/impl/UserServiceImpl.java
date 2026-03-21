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

  /**
   * Assembles and returns dashboard summary information for the given user.
   *
   * <p>If the user has an active campaign, the dashboard reflects that campaign's party stats. If
   * the user has no active campaign but has a saved party, the most recently saved party's stats
   * are shown instead. Party size counts only permanent (non-temporary) heroes.
   *
   * @param username the username of the player
   * @return a {@link DashboardInfo} populated with the user's current status
   * @throws RuntimeException if the user is not found
   */
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

    if (hasCampaign) {
      // Show the active campaign's party on the dashboard
      var activeCampaign = campaignRepository.findActiveCampaignByUsername(username);
      if (activeCampaign.isPresent()) {
        Party party = activeCampaign.get().getParty();
        partySize = (int) party.getHeroes().stream().filter(h -> !h.isTemporary()).count();
        cumulativeLevel = party.getCumulativeLevel();
        gold = party.getGold();
      }
    } else if (hasParty) {
      Party latest =
          user.getParties().stream().filter(Party::isSaved).reduce((a, b) -> b).orElse(null);
      if (latest != null) {
        partySize = (int) latest.getHeroes().stream().filter(h -> !h.isTemporary()).count();
        cumulativeLevel = latest.getCumulativeLevel();
        gold = latest.getGold();
      }
    }

    return new DashboardInfo(username, hasParty, hasCampaign, partySize, cumulativeLevel, gold);
  }

  /**
   * Returns the ID of the user with the given username.
   *
   * @param username the username to look up
   * @return the user's ID
   * @throws RuntimeException if no user exists with the given username
   */
  @Override
  public Long getUserIdByUsername(String username) {

    return userRepository
        .findByUsername(username)
        .map(User::getId)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));
  }
}
