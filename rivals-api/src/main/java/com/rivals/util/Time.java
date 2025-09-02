package com.rivals.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Time utilities for consistent timestamps.
 */
public class Time {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    /**
     * Current epoch millis (UTC).
     */
    public static long now() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Current time as ISO-8601 string (UTC).
     */
    public static String isoNow() {
        return ISO_FORMATTER.format(Instant.now());
    }

    private Time() {
        // prevent instantiation
    }
}
