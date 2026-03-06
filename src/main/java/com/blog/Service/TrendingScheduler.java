package com.blog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendingScheduler {
    private static final int TOP_K = 20;
    private final PostCacheService postCacheService;
    private final PerformanceMetricsService metricsService;

    @Scheduled(fixedRateString = "300000")
    public void rebuildTrending() {
        long start = System.currentTimeMillis();
        postCacheService.rebuildTrending(TOP_K);
        metricsService.recordLatency("scheduler.rebuildTrending", System.currentTimeMillis() - start);
    }
}
