package com.university.project.legendsofswordandwand.service.user.impl;

import com.university.project.legendsofswordandwand.dto.response.HallOfFameEntry;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo.CampaignResultInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo.HeroInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo.PartyInfo;
import com.university.project.legendsofswordandwand.dto.response.PvPStandingEntry;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.user.IProfileService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link IProfileService}, assembling user profile data and hall of fame
 * rankings.
 */
@Service
@RequiredArgsConstructor
class ProfileServiceImpl implements IProfileService {

  private final UserRepository userRepository;
  private final ICampaignProgressService campaignProgressService;
  private final CampaignRepository campaignRepository;

  /**
   * Assembles and returns the profile information for the given user.
   *
   * <p>Includes the username, PvP record, all saved party summaries, and a history of campaign
   * results ordered by score descending.
   *
   * @param username the username of the player
   * @return a {@link ProfileInfo} populated with the user's profile data
   * @throws RuntimeException if the user is not found
   */
  @Override
  public ProfileInfo getProfile(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    List<PartyInfo> partyInfoList =
        user.getParties().stream().filter(Party::isSaved).map(this::toPartyInfo).toList();

    List<CampaignResultInfo> campaignResults =
        campaignProgressService.getCampaignResultsForUser(user.getId());

    return new ProfileInfo(
        user.getUsername(), user.getPvpWins(), user.getPvpLosses(), partyInfoList, campaignResults);
  }

  /**
   * Converts a {@link Party} into a {@link PartyInfo} summary, including only permanent heroes.
   *
   * @param party the {@link Party} to convert
   * @return a {@link PartyInfo} containing the party's ID, gold, cumulative level, and heroes
   */
  private PartyInfo toPartyInfo(Party party) {
    List<HeroInfo> heroInfoList =
        party.getHeroes().stream().filter(h -> !h.isTemporary()).map(this::toHeroInfo).toList();
    return new PartyInfo(party.getId(), party.getGold(), party.getCumulativeLevel(), heroInfoList);
  }

  /**
   * Converts a {@link Hero} into a {@link HeroInfo} summary.
   *
   * <p>Uses the primary class if set, falling back to the starting class.
   *
   * @param hero the {@link Hero} to convert
   * @return a {@link HeroInfo} containing the hero's key stats and class information
   */
  private HeroInfo toHeroInfo(Hero hero) {
    return new HeroInfo(
        hero.getName(),
        hero.getPrimaryClass() != null ? hero.getPrimaryClass() : hero.getStartingClass(),
        hero.getHybridClass(),
        hero.isHybrid(),
        hero.getLevel(),
        hero.getHealth(),
        hero.getAttack(),
        hero.getDefense(),
        hero.getMana());
  }

  /**
   * Returns the top 20 hall of fame entries, ordered by campaign score descending.
   *
   * <p>Each entry includes the rank, owner's username, score, rooms completed, permanent hero
   * count, cumulative party level, and gold held at the end of the campaign.
   *
   * @return a list of up to 20 {@link HallOfFameEntry} records
   */
  @Override
  public List<HallOfFameEntry> getHallOfFame() {
    List<Campaign> top = campaignRepository.findTopScores().stream().limit(20).toList();

    List<HallOfFameEntry> entries = new ArrayList<>();
    for (int i = 0; i < top.size(); i++) {
      Campaign c = top.get(i);
      long heroCount = c.getParty().getHeroes().stream().filter(h -> !h.isTemporary()).count();
      int cumulativeLevel =
          c.getParty().getHeroes().stream()
              .filter(h -> !h.isTemporary())
              .mapToInt(Hero::getLevel)
              .sum();
      entries.add(
          new HallOfFameEntry(
              i + 1,
              c.getOwner().getUsername(),
              c.getScore(),
              c.getCurrentRoom(),
              (int) heroCount,
              cumulativeLevel,
              c.getParty().getGold()));
    }
    return entries;
  }

  @Override
  public List<PvPStandingEntry> getPvPStandings() {

    List<User> users = userRepository.findPvPStandings();
    List<PvPStandingEntry> entries = new ArrayList<>();

    for (int i = 0; i < users.size(); i++) {
      User u = users.get(i);
      int total = u.getPvpWins() + u.getPvpLosses();
      int winRate = total > 0 ? (u.getPvpWins() * 100 / total) : 0;
      entries.add(new PvPStandingEntry(
              i + 1,
              u.getUsername(),
              u.getPvpWins(),
              u.getPvpLosses(),
              total,
              winRate));
    }

    return entries;
  }
}
