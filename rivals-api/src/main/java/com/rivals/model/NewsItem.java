package com.rivals.model;

public record NewsItem(
    String id,
    String title,
    String date,
    String summary,
    String imageUrl,
    String link  // nullable
) {}
