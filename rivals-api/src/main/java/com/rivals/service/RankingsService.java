package com.rivals.service;

import com.rivals.model.RankingsPayload;
import com.rivals.repo.RankingsRepo;
import com.rivals.util.Time;
import org.springframework.stereotype.Service;

/**
 * Business logic around rankings. Fills the payload flags based on repo result.
 */
@Service
public class RankingsService {

    private final RankingsRepo repo;

    public RankingsService(RankingsRepo repo) {
        this.repo = repo;
    }

    public RankingsPayload getRankings(String region) {
        var res = repo.getRankings(region);
        boolean fallback = res.isGlobalFallback();
        String note = fallback ? "Showing Global Top 10 (regional data unavailable)" : null;

        // updatedAt can be now; rows also carry their own updatedAt fields
        return new RankingsPayload(
                Time.isoNow(),
                res.players(),
                res.requestedRegion(),
                res.effectiveRegion(),
                fallback,
                note
        );
    }
}
