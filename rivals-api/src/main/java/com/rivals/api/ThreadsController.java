package com.rivals.api;

import com.rivals.model.CommentDto;
import com.rivals.model.PageComment;
import com.rivals.model.PageThread;
import com.rivals.model.ThreadDto;
import com.rivals.service.ThreadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Threads + Comments endpoints.
 */
@RestController
@RequestMapping("/threads")
@Validated
public class ThreadsController {

    private final ThreadService service;

    public ThreadsController(ThreadService service) {
        this.service = service;
    }

    /* ---------- Threads ---------- */

    @GetMapping
    public PageThread listThreads(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "25") @Min(1) Integer limit
    ) {
        return service.listThreads(cursor, limit);
    }

    /** Local MVP: accept title (+ optional body as initial post) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThreadDto createThread(
            HttpServletRequest request,
            @Valid @RequestBody CreateThreadRequest body
    ) {
        return service.createThread(request, body.title(), body.authorId(), body.body());
    }

    @GetMapping("/{threadId}")
    public ThreadDto getThread(@PathVariable String threadId) {
        return service.getThread(threadId);
    }

    /* ---------- Comments ---------- */

    @GetMapping("/{threadId}/comments")
    public PageComment listComments(
            @PathVariable String threadId,
            @RequestParam(required = false) Long since,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") @Min(1) Integer limit
    ) {
        return service.listComments(threadId, since, cursor, limit);
    }

    @PostMapping("/{threadId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            HttpServletRequest request,
            @PathVariable String threadId,
            @Valid @RequestBody AddCommentRequest body
    ) {
        return service.addComment(request, threadId, body.authorId(), body.body());
    }

    /* ---------- Request bodies ---------- */

    public record CreateThreadRequest(
            @NotBlank String title,
            String body,
            String authorId // optional; can also be provided via headers for real auth
    ) {}

    public record AddCommentRequest(
            @NotBlank String body,
            String authorId // optional
    ) {}
}
