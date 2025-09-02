package com.rivals.model;

import java.util.List;

public record NewsPayload(
    List<NewsItem> items
) {}
