package com.university.project.legendsofswordandwand.dto.response;

public record PvPStandingEntry(
        int rank,
        String username,
        int wins,
        int losses,
        int totalGames,
        int winRate) {}
