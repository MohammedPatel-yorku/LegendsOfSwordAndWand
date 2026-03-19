package com.university.project.legendsofswordandwand.dto.response;

public record HallOfFameEntry(
    int rank,
    String username,
    int score,
    int roomsReached,
    int heroCount,
    int cumulativeLevel,
    int gold) {}
