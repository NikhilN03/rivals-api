package com.rivals.api;

import com.rivals.service.ThreadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Comment mutations that aren’t thread-scoped. */
@RestController
@RequestMapping("/comments")
public class CommentsController {

    private final ThreadService threads;

    public CommentsController(ThreadService threads) {
        this.threads = threads;
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> like(@PathVariable String commentId) {
        threads.likeComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
