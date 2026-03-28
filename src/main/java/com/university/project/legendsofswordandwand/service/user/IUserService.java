package com.university.project.legendsofswordandwand.service.user;

import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;

/**
 * Service interface defining the contract for general user data retrieval used by the dashboard.
 */
public interface IUserService {

  /**
   * Assembles the dashboard view data for the given user, including campaign and party status.
   *
   * @param username the username of the logged-in player
   * @return a {@link DashboardInfo} DTO with the player's current game state summary
   */
  DashboardInfo getDashboardInfo(String username);

  /**
   * Looks up the database ID for the given username.
   *
   * @param username the username to look up
   * @return the user's database ID
   * @throws RuntimeException if the username is not found
   */
  Long getUserIdByUsername(String username);
}
