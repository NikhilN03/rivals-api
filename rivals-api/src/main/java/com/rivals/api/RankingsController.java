package com.rivals.api;

import com.rivals.model.RankingsPayload;
import com.rivals.service.RankingsService;
import org.springframework.web.bind.annotation.*;

/** Rankings endpoint (Global + Regional with fallback flags). */
@RestController
@RequestMapping("/rankings")
public class RankingsController {

    private final RankingsService service;

    public RankingsController(RankingsService service) {
        this.service = service;
    }

    @GetMapping
    public RankingsPayload get(@RequestParam(defaultValue = "GLOBAL") String region) {
        return service.getRankings(region);
    }
}
