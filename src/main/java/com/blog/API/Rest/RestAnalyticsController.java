package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.Model.Post;
import com.blog.Service.AnalyticsService;
import com.blog.Service.PerformanceMetricsService;
import com.blog.Service.PostCacheService;
import com.blog.Service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Analytics & Metrics", description = "Performance analytics and runtime metrics endpoints")
public class RestAnalyticsController {
    private final AnalyticsService analyticsService;
    private final PerformanceMetricsService metricsService;
    private final PostCacheService postCacheService;
    private final PostService postService;

    @GetMapping("/analytics/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Async analytics dashboard", description = "Parallel CompletableFuture aggregation of blog statistics")
    public CompletableFuture<ResponseEntity<SuccessResponse<Map<String, Object>>>> dashboard() {
        metricsService.incrementCounter("api.analytics.dashboard");
        return analyticsService.buildDashboard().thenApply(data -> ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Analytics dashboard", data)));
    }
    @GetMapping("/analytics/trending")
    @Operation(summary = "Trending posts", description = "Top-N posts by view count (O(n log k) min-heap algorithm)")
    public ResponseEntity<SuccessResponse<List<Post>>> trending(@RequestParam(defaultValue = "10") int limit) {
        metricsService.incrementCounter("api.analytics.trending");
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Trending posts", postService.getTrending(limit)));
    }
    @GetMapping("/analytics/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Runtime metrics snapshot", description = "Latency percentiles, request counts, heap usage, and CPU load")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> metrics() {
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Runtime metrics snapshot", metricsService.snapshot()));
    }
    @GetMapping("/posts/search")
    @Operation(summary = "Search posts", description = "O(n) keyword search over in-memory post index (title + body)")
    public ResponseEntity<SuccessResponse<List<Post>>> search(@RequestParam String keyword) {
        metricsService.incrementCounter("api.posts.search");
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Search results for: " + keyword, postService.search(keyword)));
    }
    @PostMapping("/posts/{id}/view")
    @Operation(summary = "Record a post view", description = "Thread-safe AtomicLong increment in ConcurrentHashMap")
    public ResponseEntity<SuccessResponse<Long>> recordView(@PathVariable int id) {
        long views = postCacheService.incrementViews(id);
        metricsService.incrementCounter("api.posts.view");
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "View recorded", views));
    }
}
