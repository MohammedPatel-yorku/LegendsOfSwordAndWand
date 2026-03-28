package com.university.project.legendsofswordandwand.service.user;

import com.university.project.legendsofswordandwand.dto.response.HallOfFameEntry;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.dto.response.PvPStandingEntry;
import java.util.List;

/** Service interface defining the contract for profile and leaderboard data retrieval. */
public interface IProfileService {

  /**
   * Assembles the full profile view for the given user, including saved parties, heroes, PvP
   * record, and campaign history.
   *
   * @param username the username of the player
   * @return a {@link ProfileInfo} DTO
   * @throws RuntimeException if the user is not found
   */
  ProfileInfo getProfile(String username);

  /**
   * Returns the Hall of Fame leaderboard, ranking all completed campaigns by final score in
   * descending order.
   *
   * @return a ranked list of {@link HallOfFameEntry} records
   */
  List<HallOfFameEntry> getHallOfFame();

  /**
   * Returns the PvP league standings, ranking all players by win count, then win rate.
   *
   * @return a ranked list of {@link PvPStandingEntry} records
   */
  List<PvPStandingEntry> getPvPStandings();
}
