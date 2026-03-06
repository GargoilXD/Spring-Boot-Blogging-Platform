package com.blog.Service;

import com.blog.Model.Post;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class PostCacheService {
    private final ConcurrentHashMap<Integer, Post> postIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, AtomicLong> viewCounts = new ConcurrentHashMap<>();
    private volatile CopyOnWriteArrayList<Post> trendingSnapshot = new CopyOnWriteArrayList<>();
    public void put(Post post) {
        postIndex.put(post.getId(), post);
        viewCounts.putIfAbsent(post.getId(), new AtomicLong(0));
    }
    public Optional<Post> get(int postId) {
        return Optional.ofNullable(postIndex.get(postId));
    }
    public void evict(int postId) {
        postIndex.remove(postId);
        viewCounts.remove(postId);
    }
    public List<Post> allCached() {
        return List.copyOf(postIndex.values());
    }
    public long incrementViews(int postId) {
        return viewCounts.computeIfAbsent(postId, id -> new AtomicLong(0)).incrementAndGet();
    }
    public long viewCount(int postId) {
        AtomicLong counter = viewCounts.get(postId);
        return counter == null ? 0L : counter.get();
    }
    public void rebuildTrending(int topK) {
        PriorityQueue<Map.Entry<Integer, Long>> heap = new PriorityQueue<>(Comparator.comparingLong(Map.Entry::getValue));
        for (Map.Entry<Integer, AtomicLong> entry : viewCounts.entrySet()) {
            long views = entry.getValue().get();
            heap.offer(Map.entry(entry.getKey(), views));
            if (heap.size() > topK) {
                heap.poll();
            }
        }
        List<Post> trending = new ArrayList<>();
        while (!heap.isEmpty()) {
            int id = heap.poll().getKey();
            Post post = postIndex.get(id);
            if (post != null) trending.add(post);
        }
        Collections.reverse(trending);
        trendingSnapshot = new CopyOnWriteArrayList<>(trending);
    }
    public List<Post> getTrending() {
        return Collections.unmodifiableList(trendingSnapshot);
    }
    public List<Post> sortedByDateDesc() {
        return postIndex.values().stream()
                .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
    public List<Post> sortedByTitle() {
        return postIndex.values().stream().sorted(Comparator.comparing(p -> p.getTitle().toLowerCase())).collect(Collectors.toList());
    }
    public List<Post> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return allCached();
        String lower = keyword.toLowerCase();
        return postIndex.values().stream().filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(lower)) || (p.getBody()  != null && p.getBody().toLowerCase().contains(lower))).collect(Collectors.toList());
    }
    public int size() {
        return postIndex.size();
    }
}
