package com.rivals.repo;
import org.springframework.stereotype.Repository;
import com.rivals.model.NewsItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory curated news for MVP. Replace with a fetcher later.
 */
public class NewsRepo {

    private final List<NewsItem> items = new ArrayList<>();

    public NewsRepo() {
        // Seed 4 stories (you can edit these at runtime or expose an admin endpoint later)
        items.add(new NewsItem(
                "s1",
                "Marvel Rivals: Week in Review",
                "Aug 28",
                "Top roster moves, standout plays, and meta shifts from this week’s scrims and tournaments.",
                "/images/placeholder.png",
                "#"
        ));
        items.add(new NewsItem(
                "s2",
                "Patch 1.0: What changed for your mains?",
                "Aug 27",
                "A simple rundown of buffs/nerfs and what they mean for competitive play.",
                "/images/placeholder.png",
                "#"
        ));
        items.add(new NewsItem(
                "s3",
                "Community Spotlight: Rising teams to watch",
                "Aug 26",
                "Five rosters making waves in scrims — who’s peaking just in time for qualifiers?",
                "/images/placeholder.png",
                "#"
        ));
        items.add(new NewsItem(
                "s4",
                "Esports roadmap: What’s next this season",
                "Aug 25",
                "Qualifier dates, format expectations, and where to catch official broadcasts.",
                "/images/placeholder.png",
                "#"
        ));
    }

    public List<NewsItem> list() {
        return Collections.unmodifiableList(items);
    }

    public void replaceAll(List<NewsItem> newItems) {
        items.clear();
        items.addAll(newItems);
    }
}
