package com.rivals.util;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * Utility for generating ULIDs (Universally Unique Lexicographically Sortable Identifiers).
 */
public class Ids {

    /**
     * Generate a new ULID string.
     */
    public static String ulid() {
        return UlidCreator.getUlid().toString();
    }

    private Ids() {
        // prevent instantiation
    }
}
