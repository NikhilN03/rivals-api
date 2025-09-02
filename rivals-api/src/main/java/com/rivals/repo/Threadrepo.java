package com.rivals.repo;

import org.springframework.stereotype.Repository;
import com.rivals.model.CommentDto;
import com.rivals.model.PageComment;
import com.rivals.model.PageThread;
import com.rivals.model.ThreadDto;
import com.rivals.util.CursorCodec;
import com.rivals.util.Ids;
import com.rivals.util.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * In-memory repository for threads & comments.
 * - Threads ordered by lastActivityAt (DESC) using a descending view of a time-based key.
 * - Comments stored per-thread in a NavigableMap keyed by composite "%013d#%s".
 * - Cursor is opaque (base64 of the last composite key seen).
 */
public class ThreadRepo {

    /** id -> thread */
    private final ConcurrentHashMap<String, ThreadDto> threads = new ConcurrentHashMap<>();

    /** threadId -> ordered comments (by createdAt asc) */
    private final ConcurrentHashMap<String, NavigableMap<String, CommentDto>> commentsByThread =
            new ConcurrentHashMap<>();

    /** commentId -> (threadId, compositeKey) for fast locate/like */
    private final ConcurrentHashMap<String, CommentLocator> commentIndex = new ConcurrentHashMap<>();

    /** composite "%013d#<threadId>" (lastActivityAt) -> threadId (ascending map, read via descending view) */
    private final ConcurrentSkipListMap<String, String> threadOrder = new ConcurrentSkipListMap<>();

    private static String compositeKey(long epochMs, String ulidOrId) {
        return String.format("%013d#%s", epochMs, ulidOrId);
    }

    /* --------------------------- Threads --------------------------- */

    public synchronized ThreadDto createThread(String title, String authorId, String body) {
        Objects.requireNonNull(title, "title");
        final String id = Ids.ulid();
        final long now = Time.now();

        ThreadDto t = new ThreadDto(id, title, authorId, now, now, 0);
        threads.put(id, t);
        threadOrder.put(compositeKey(now, id), id);
        commentsByThread.putIfAbsent(id, new ConcurrentSkipListMap<>());

        // If body is provided, create the first comment (typical forum UX)
        if (body != null && !body.isBlank()) {
            addComment(id, authorId, body);
        }
        return t;
    }

    public ThreadDto getThread(String id) {
        return threads.get(id);
    }

    /** List threads ordered by lastActivityAt DESC using cursor pagination. */
    public PageThread listThreads(String cursor, int limit) {
        if (limit <= 0) limit = 25;

        final String startKey = CursorCodec.decode(cursor); // may be null
        NavigableMap<String, String> desc = threadOrder.descendingMap();

        if (startKey != null) {
            // headMap in DESC view returns entries BEFORE 'startKey' in descending order (exclusive)
            desc = desc.headMap(startKey, false);
        }

        final List<ThreadDto> items = new ArrayList<>(Math.min(limit, desc.size()));
        String lastEmittedKey = null;

        int count = 0;
        for (Map.Entry<String, String> e : desc.entrySet()) {
            if (count >= limit) break;
            ThreadDto t = threads.get(e.getValue());
            if (t != null) {
                items.add(t);
                lastEmittedKey = e.getKey();
                count++;
            }
        }

        // If there are more entries beyond the ones we returned, emit a cursor
        String nextCursor = null;
        if (lastEmittedKey != null) {
            NavigableMap<String, String> remaining = desc.headMap(lastEmittedKey, false);
            if (!remaining.isEmpty()) {
                nextCursor = CursorCodec.encode(lastEmittedKey);
            }
        }

        return new PageThread(items, nextCursor);
    }

    /* --------------------------- Comments --------------------------- */

    public synchronized CommentDto addComment(String threadId, String authorId, String body) {
        Objects.requireNonNull(threadId, "threadId");
        Objects.requireNonNull(body, "body");

        ThreadDto existing = threads.get(threadId);
        if (existing == null) {
            throw new IllegalArgumentException("Thread not found: " + threadId);
        }

        final long now = Time.now();
        final String commentId = Ids.ulid();
        final String compKey = compositeKey(now, commentId);

        NavigableMap<String, CommentDto> map =
                commentsByThread.computeIfAbsent(threadId, k -> new ConcurrentSkipListMap<>());
        CommentDto newComment = new CommentDto(commentId, threadId, authorId, body, now, 0);
        map.put(compKey, newComment);
        commentIndex.put(commentId, new CommentLocator(threadId, compKey));

        // Update thread lastActivityAt and postCount; adjust ordering map
        final String oldThreadKey = compositeKey(existing.lastActivityAt(), existing.id());
        threadOrder.remove(oldThreadKey);

        ThreadDto updated = new ThreadDto(
                existing.id(),
                existing.title(),
                existing.authorId(),
                existing.createdAt(),
                now,
                existing.postCount() + 1
        );
        threads.put(updated.id(), updated);
        threadOrder.put(compositeKey(updated.lastActivityAt(), updated.id()), updated.id());

        return newComment;
    }

    /** List comments chronologically (ASC) with since + cursor support. */
    public PageComment listComments(String threadId, Long since, String cursor, int limit) {
        if (limit <= 0) limit = 50;

        NavigableMap<String, CommentDto> all =
                commentsByThread.getOrDefault(threadId, new ConcurrentSkipListMap<>());

        NavigableMap<String, CommentDto> view = all;

        if (since != null && since > 0) {
            String sinceKey = compositeKey(since, ""); // minimal suffix
            view = view.tailMap(sinceKey, true);
        }

        final String startKey = CursorCodec.decode(cursor);
        if (startKey != null) {
            view = view.tailMap(startKey, false);
        }

        final List<CommentDto> items = new ArrayList<>(Math.min(limit, view.size()));
        String lastEmittedKey = null;
        int count = 0;

        for (Map.Entry<String, CommentDto> e : view.entrySet()) {
            if (count >= limit) break;
            items.add(e.getValue());
            lastEmittedKey = e.getKey();
            count++;
        }

        String nextCursor = null;
        if (lastEmittedKey != null) {
            NavigableMap<String, CommentDto> remaining = view.tailMap(lastEmittedKey, false);
            if (!remaining.isEmpty()) {
                nextCursor = CursorCodec.encode(lastEmittedKey);
            }
        }

        return new PageComment(items, nextCursor);
    }

    /** Increment like count on a comment; returns true if found. */
    public synchronized boolean likeComment(String commentId) {
        CommentLocator loc = commentIndex.get(commentId);
        if (loc == null) return false;

        NavigableMap<String, CommentDto> map = commentsByThread.get(loc.threadId());
        if (map == null) return false;

        CommentDto current = map.get(loc.compositeKey());
        if (current == null) return false;

        CommentDto updated = new CommentDto(
                current.id(),
                current.threadId(),
                current.authorId(),
                current.body(),
                current.createdAt(),
                current.likes() + 1
        );
        map.put(loc.compositeKey(), updated);
        return true;
    }

    /* --------------------------- Helpers --------------------------- */

    private record CommentLocator(String threadId, String compositeKey) {}

    // Convenience seeders for tests/dev
    public synchronized void clearAll() {
        threads.clear();
        commentsByThread.clear();
        commentIndex.clear();
        threadOrder.clear();
    }

    public List<ThreadDto> allThreadsUnsafe() {
        return Collections.unmodifiableList(new ArrayList<>(threads.values()));
    }
}
