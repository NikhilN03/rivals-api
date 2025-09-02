package com.rivals.rate;

/**
 * Model returned by /me/limits to describe the current daily allowance.
 * subject: "ANON" or "USER"
 * remaining: remaining tokens for today
 * limit: daily limit
 * resetAt: epoch millis when the bucket resets (next midnight UTC)
 */
public record Allowance(
        String subject,
        int remaining,
        int limit,
        long resetAt
) {}
