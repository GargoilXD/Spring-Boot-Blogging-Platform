package com.blog.Service;

import com.blog.Model.Post;
import com.blog.Repository.CommentRepository;
import com.blog.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostCacheService postCacheService;
    private final PerformanceMetricsService metricsService;

    @Async("analyticsExecutor")
    public CompletableFuture<Long> countPostsAsync() {
        long start = System.currentTimeMillis();
        long count = postRepository.count();
        metricsService.recordLatency("analytics.countPosts", System.currentTimeMillis() - start);
        return CompletableFuture.completedFuture(count);
    }
    @Async("analyticsExecutor")
    public CompletableFuture<Long> countCommentsAsync() {
        long start = System.currentTimeMillis();
        long count = commentRepository.count();
        metricsService.recordLatency("analytics.countComments", System.currentTimeMillis() - start);
        return CompletableFuture.completedFuture(count);
    }
    @Async("analyticsExecutor")
    public CompletableFuture<List<Post>> fetchTrendingPostsAsync(int topK) {
        long start = System.currentTimeMillis();
        postCacheService.rebuildTrending(topK);
        List<Post> trending = postCacheService.getTrending();
        metricsService.recordLatency("analytics.trending", System.currentTimeMillis() - start);
        return CompletableFuture.completedFuture(trending);
    }
    @Async("analyticsExecutor")
    public CompletableFuture<List<String>> recentPostSummariesAsync(int limit) {
        List<Post> recent = postRepository.findAll(PageRequest.of(0, limit)).getContent();
        List<String> summaries = recent.parallelStream().map(p -> String.format("[%d] %s", p.getId(), truncate(p.getTitle(), 60))).collect(Collectors.toList());
        return CompletableFuture.completedFuture(summaries);
    }
    public CompletableFuture<Map<String, Object>> buildDashboard() {
        long wallStart = System.currentTimeMillis();
        CompletableFuture<Long> postCountFuture = countPostsAsync();
        CompletableFuture<Long> commentCountFuture = countCommentsAsync();
        CompletableFuture<List<Post>> trendingFuture = fetchTrendingPostsAsync(10);
        CompletableFuture<List<String>> recentFuture = recentPostSummariesAsync(5);
        return CompletableFuture
                .allOf(postCountFuture, commentCountFuture, trendingFuture, recentFuture)
                .thenApply(ignored -> {
                    long wallMs = System.currentTimeMillis() - wallStart;
                    metricsService.recordLatency("analytics.dashboard", wallMs);
                    Map<String, Object> dashboard = new LinkedHashMap<>();
                    dashboard.put("generatedAt", Instant.now().toString());
                    dashboard.put("totalPosts", postCountFuture.join());
                    dashboard.put("totalComments", commentCountFuture.join());
                    dashboard.put("trendingPosts", trendingFuture.join().stream()
                            .map(p -> Map.of(
                                    "id",    p.getId(),
                                    "title", truncate(p.getTitle(), 60),
                                    "views", postCacheService.viewCount(p.getId())))
                            .collect(Collectors.toList()));
                    dashboard.put("recentPostSummaries",   recentFuture.join());
                    dashboard.put("dashboardBuildMs",      wallMs);
                    return dashboard;
                });
    }
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
