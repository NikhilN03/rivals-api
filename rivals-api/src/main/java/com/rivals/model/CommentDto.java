package com.rivals.model;

public record CommentDto(
    String id,
    String threadId,
    String authorId,  // nullable
    String body,
    long createdAt,
    int likes
) {}
