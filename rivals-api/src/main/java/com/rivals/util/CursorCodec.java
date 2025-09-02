package com.rivals.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encode/decode opaque cursors for pagination.
 * For now, we just wrap the last key in Base64.
 */
public class CursorCodec {

    /**
     * Encode a key (e.g. last seen item) into a Base64 cursor.
     */
    public static String encode(String lastKey) {
        if (lastKey == null) return null;
        return Base64.getUrlEncoder().encodeToString(lastKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a Base64 cursor back into its original key.
     */
    public static String decode(String cursor) {
        if (cursor == null) return null;
        return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
    }

    private CursorCodec() {
        // prevent instantiation
    }
}
