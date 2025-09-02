package com.rivals.api;

import com.rivals.rate.Allowance;
import com.rivals.rate.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/** Exposes current daily allowance (anon vs user). */
@RestController
@RequestMapping("/me")
public class MeController {

    private final RateLimiter limiter;

    public MeController(RateLimiter limiter) {
        this.limiter = limiter;
    }

    @GetMapping("/limits")
    public Allowance limits(HttpServletRequest request) {
        return limiter.getAllowance(request);
    }
}
