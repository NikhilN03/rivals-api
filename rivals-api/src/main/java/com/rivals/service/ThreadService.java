package com.rivals.service;

import com.rivals.model.CommentDto;
import com.rivals.model.PageComment;
import com.rivals.model.PageThread;
import com.rivals.model.ThreadDto;
import com.rivals.rate.RateLimiter;
import com.rivals.repo.ThreadRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Business logic for threads & comments.
 * - Wraps ThreadRepo.
 * - Enforces daily rate limits on creating threads and adding comments.
 */
@Service
public class ThreadService {

    private final ThreadRepo repo;
    private final RateLimiter rateLimiter;

    public ThreadService(ThreadRepo repo, RateLimiter rateLimiter) {
        this.repo = repo;
        this.rateLimiter = rateLimiter;
    }

    /* -------------------- Threads -------------------- */

    public PageThread listThreads(String cursor, Integer limit) {
        int lim = (limit == null || limit <= 0) ? 25 : limit;
        return repo.listThreads(cursor, lim);
    }

    /** Create a thread; counts against daily posting allowance. */
    public ThreadDto createThread(HttpServletRequest request, String title, String authorId, String body) {
        enforceAllowance(request);
        return repo.createThread(title, authorId, body);
    }

    public ThreadDto getThread(String threadId) {
        ThreadDto t = repo.getThread(threadId);
        if (t == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found: " + threadId);
        }
        return t;
    }

    /* -------------------- Comments -------------------- */

    public PageComment listComments(String threadId, Long since, String cursor, Integer limit) {
        int lim = (limit == null || limit <= 0) ? 50 : limit;
        return repo.listComments(threadId, since, cursor, lim);
    }

    /** Add a comment; counts against daily posting allowance. */
    public CommentDto addComment(HttpServletRequest request, String threadId, String authorId, String body) {
        enforceAllowance(request);
        return repo.addComment(threadId, authorId, body);
    }

    public void likeComment(String commentId) {
        boolean ok = repo.likeComment(commentId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: " + commentId);
        }
    }

    /* -------------------- Helpers -------------------- */

    private void enforceAllowance(HttpServletRequest request) {
        if (!rateLimiter.tryConsume(request, 1)) {
            // 429 Too Many Requests; controllers can let this bubble up
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily post limit exceeded");
        }
    }
}
