package com.rivals.service;

import com.rivals.model.NewsPayload;
import com.rivals.repo.NewsRepo;
import org.springframework.stereotype.Service;

/**
 * Simple wrapper for curated news list.
 */
@Service
public class NewsService {

    private final NewsRepo repo;

    public NewsService(NewsRepo repo) {
        this.repo = repo;
    }

    public NewsPayload listNews() {
        return new NewsPayload(repo.list());
    }
}
