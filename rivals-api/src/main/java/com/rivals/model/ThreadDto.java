package com.rivals.model;

public record ThreadDto(
    String id,
    String title,
    String authorId,  // nullable
    long createdAt,
    long lastActivityAt,
    int postCount
) {}
