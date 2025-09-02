package com.rivals.api;

import com.rivals.model.NewsPayload;
import com.rivals.service.NewsService;
import org.springframework.web.bind.annotation.*;

/** Curated news for MVP (static list for now). */
@RestController
@RequestMapping("/news")
public class NewsController {

    private final NewsService service;

    public NewsController(NewsService service) {
        this.service = service;
    }

    @GetMapping
    public NewsPayload list() {
        return service.listNews();
    }
}
