package com.rivals.repo;

import org.springframework.stereotype.Repository;
import com.rivals.model.RankingRow;
import com.rivals.util.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rankings by region.
 * For MVP we seed GLOBAL only; other regions return GLOBAL with a fallback flag.
 */
public class RankingsRepo {

    private static final String GLOBAL = "GLOBAL";

    private final ConcurrentHashMap<String, List<RankingRow>> byRegion = new ConcurrentHashMap<>();

    public RankingsRepo() {
        seedGlobal();
    }

    private void seedGlobal() {
        String iso = Time.isoNow();
        List<RankingRow> rows = new ArrayList<>();
        rows.add(new RankingRow("p1", "Crimson", 1, 2987, "US", "", 0.66, 3.10, 164, iso));
        rows.add(new RankingRow("p2", "Zenith",  2, 2930, "SE", "", 0.63, 2.80, 157, iso));
        rows.add(new RankingRow("p3", "Katana",  3, 2895, "JP", "", 0.61, 2.70, 152, iso));
        rows.add(new RankingRow("p4", "Nova",    4, 2872, "KR", "", 0.60, 2.60, 149, iso));
        rows.add(new RankingRow("p5", "Marauder",5, 2830, "BR", "", 0.59, 2.50, 147, iso));
        rows.add(new RankingRow("p6", "Valkyrie",6, 2811, "DE", "", 0.58, 2.40, 144, iso));
        rows.add(new RankingRow("p7", "Echo",    7, 2795, "GB", "", 0.57, 2.30, 142, iso));
        rows.add(new RankingRow("p8", "Spectre", 8, 2782, "CA", "", 0.57, 2.30, 141, iso));
        rows.add(new RankingRow("p9", "Quasar",  9, 2769, "FR", "", 0.56, 2.20, 139, iso));
        rows.add(new RankingRow("p10","Falcon", 10, 2755, "US", "", 0.55, 2.10, 137, iso));
        byRegion.put(GLOBAL, rows);
    }

    /** Simple result object for controller to build payload flags easily. */
    public static final class RegionResult {
        private final String requestedRegion;
        private final String effectiveRegion;
        private final boolean globalFallback;
        private final List<RankingRow> players;

        public RegionResult(String requestedRegion, String effectiveRegion, boolean globalFallback, List<RankingRow> players) {
            this.requestedRegion = requestedRegion;
            this.effectiveRegion = effectiveRegion;
            this.globalFallback = globalFallback;
            this.players = players;
        }
        public String requestedRegion() { return requestedRegion; }
        public String effectiveRegion() { return effectiveRegion; }
        public boolean isGlobalFallback() { return globalFallback; }
        public List<RankingRow> players() { return players; }
    }

    /** Get rankings for region; fall back to GLOBAL if missing. */
    public RegionResult getRankings(String region) {
        String req = (region == null || region.isBlank()) ? GLOBAL : region.toUpperCase();
        List<RankingRow> list = byRegion.get(req);
        if (list != null && !list.isEmpty()) {
            return new RegionResult(req, req, false, Collections.unmodifiableList(list));
        }
        List<RankingRow> global = byRegion.getOrDefault(GLOBAL, List.of());
        return new RegionResult(req, GLOBAL, true, Collections.unmodifiableList(global));
    }

    /** For future scrapes/updates. */
    public void putRegion(String region, List<RankingRow> players) {
        byRegion.put(region.toUpperCase(), new ArrayList<>(players));
    }

    public Map<String, List<RankingRow>> snapshot() {
        return Collections.unmodifiableMap(byRegion);
    }
}
