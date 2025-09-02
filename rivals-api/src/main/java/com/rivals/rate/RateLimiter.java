package com.rivals.rate;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Very small in-memory, per-day (UTC) rate limiter.
 *
 * Keys:
 *  - ANON#<ip>   for anonymous callers (3/day)
 *  - USER#<user> for authenticated callers (7/day)
 *
 * User id detection (local dev):
 *  - Header "X-User-Id" or "X-Debug-User" indicates an authenticated subject.
 *  - Otherwise treated as ANON with IP-based key.
 */
public class RateLimiter {

    private static final int ANON_LIMIT = 3;
    private static final int USER_LIMIT = 7;

    private static final String SUBJECT_ANON = "ANON";
    private static final String SUBJECT_USER = "USER";

    /** key -> per-day counter */
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    /* ------------------------- Public API ------------------------- */

    /**
     * Consume "tokens" if available for this request subject.
     * @return true if allowed and consumed; false if limit exceeded
     */
    public boolean tryConsume(HttpServletRequest req, int tokens) {
        Objects.requireNonNull(req, "request");

        final Subject subj = resolveSubject(req);
        final String key = subjectKey(subj);
        final int limit = subj.isUser() ? USER_LIMIT : ANON_LIMIT;

        final AtomicBoolean allowed = new AtomicBoolean(false);
        counters.compute(key, (k, c) -> {
            final LocalDate today = LocalDate.now(ZoneOffset.UTC);
            if (c == null || !c.day().equals(today) || c.limit() != limit) {
                c = new Counter(today, 0, limit);
            }
            if (c.used() + tokens <= c.limit()) {
                allowed.set(true);
                return new Counter(today, c.used() + tokens, limit);
            }
            return c; // unchanged; over limit
        });

        return allowed.get();
    }

    /**
     * Return the current allowance (remaining, limit, resetAt) for this subject.
     */
    public Allowance getAllowance(HttpServletRequest req) {
        Objects.requireNonNull(req, "request");

        final Subject subj = resolveSubject(req);
        final String key = subjectKey(subj);
        final int limit = subj.isUser() ? USER_LIMIT : ANON_LIMIT;

        final LocalDate today = LocalDate.now(ZoneOffset.UTC);
        final Counter c = counters.compute(key, (k, existing) -> {
            if (existing == null || !existing.day().equals(today) || existing.limit() != limit) {
                return new Counter(today, 0, limit);
            }
            return existing;
        });

        final int remaining = Math.max(0, c.limit() - c.used());
        final long resetAt = c.day().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();

        return new Allowance(subj.isUser() ? SUBJECT_USER : SUBJECT_ANON, remaining, c.limit(), resetAt);
    }

    /* ------------------------- Helpers ------------------------- */

    private record Counter(LocalDate day, int used, int limit) {}

    /** Represents the resolved caller: user (with id) or anonymous (with ip). */
    private static final class Subject {
        private final String userId; // non-null if user
        private final String ip;     // non-null if anon

        private Subject(String userId, String ip) {
            this.userId = userId;
            this.ip = ip;
        }

        static Subject user(String id)   { return new Subject(Objects.requireNonNull(id), null); }
        static Subject anon(String ip)   { return new Subject(null, Objects.requireNonNull(ip)); }
        boolean isUser()                 { return userId != null; }
        String key()                     { return isUser() ? "USER#" + userId : "ANON#" + ip; }
    }

    private static String subjectKey(Subject s) {
        return s.key();
    }

    /**
     * Resolve subject from request:
     *  - user id from "X-User-Id" or "X-Debug-User" header (local dev)
     *  - else anonymous using client IP (X-Forwarded-For first, then remoteAddr)
     */
    private static Subject resolveSubject(HttpServletRequest req) {
        String userId = headerFirstNonBlank(req, "X-User-Id", "X-Debug-User");
        if (userId != null) {
            return Subject.user(userId);
        }
        String ip = clientIp(req);
        return Subject.anon(ip);
    }

    /** Pick first non-blank header value from the candidates. */
    private static String headerFirstNonBlank(HttpServletRequest req, String... names) {
        for (String h : names) {
            String v = req.getHeader(h);
            if (v != null) {
                v = v.trim();
                if (!v.isEmpty()) return v;
            }
        }
        return null;
    }

    /**
     * Get client IP, preferring X-Forwarded-For (first entry) if present,
     * otherwise HttpServletRequest#getRemoteAddr().
     */
    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty()) return first;
        }
        return Optional.ofNullable(req.getRemoteAddr()).orElse("0.0.0.0");
    }
}
