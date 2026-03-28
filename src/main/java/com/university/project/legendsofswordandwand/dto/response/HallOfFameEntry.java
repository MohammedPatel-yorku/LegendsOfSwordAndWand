package com.university.project.legendsofswordandwand.dto.response;

/**
 * Immutable DTO representing one entry in the Hall of Fame leaderboard. Entries are ranked by final
 * campaign score in descending order.
 *
 * @param rank the player's position on the leaderboard (1 = highest score)
 * @param username the player's username
 * @param score the final score achieved in their best campaign run
 * @param roomsReached the number of rooms completed in that run
 * @param heroCount the number of heroes alive at the end of the campaign
 * @param cumulativeLevel the sum of all surviving hero levels at campaign end
 * @param gold the gold remaining at campaign end
 */
public record HallOfFameEntry(
    int rank,
    String username,
    int score,
    int roomsReached,
    int heroCount,
    int cumulativeLevel,
    int gold) {}
