package com.rivals.model;

public record RankingRow(
    String playerId,
    String playerName,
    int rank,
    int rating,
    String countryCode,
    String avatarUrl,
    double winRate,
    double kda,
    int adr,
    String updatedAt  // ISO format
) {}
