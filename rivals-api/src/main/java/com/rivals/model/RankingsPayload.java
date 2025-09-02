package com.rivals.model;

import java.util.List;

public record RankingsPayload(
    String updatedAt,
    List<RankingRow> players,
    String requestedRegion,  // optional
    String effectiveRegion,  // optional
    Boolean isGlobalFallback,  // optional
    String note  // optional
) {}
