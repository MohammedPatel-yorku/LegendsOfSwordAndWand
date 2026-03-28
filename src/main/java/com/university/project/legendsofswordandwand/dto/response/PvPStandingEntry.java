package com.university.project.legendsofswordandwand.dto.response;

/**
 * Immutable DTO representing one entry in the PvP league standings. Entries are ranked by win
 * count, then win rate.
 *
 * @param rank the player's position in the league (1 = most wins)
 * @param username the player's username
 * @param wins the number of PvP battles won
 * @param losses the number of PvP battles lost
 * @param totalGames the total number of PvP battles played ({@code wins + losses})
 * @param winRate the win rate as a percentage (0–100), or 0 if no games played
 */
public record PvPStandingEntry(
    int rank, String username, int wins, int losses, int totalGames, int winRate) {}
