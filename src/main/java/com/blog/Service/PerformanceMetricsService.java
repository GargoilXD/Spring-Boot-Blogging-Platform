package com.blog.Service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PerformanceMetricsService {
    private static final int MAX_SAMPLES = 1000;
    private final ConcurrentHashMap<String, ArrayDeque<Long>> latencySamples = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> requestCounts  = new ConcurrentHashMap<>();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    public void recordLatency(String operation, long millis) {
        latencySamples.computeIfAbsent(operation, k -> new ArrayDeque<>(MAX_SAMPLES));
        ArrayDeque<Long> samples = latencySamples.get(operation);
        synchronized (samples) {
            if (samples.size() >= MAX_SAMPLES) samples.pollFirst();
            samples.addLast(millis);
        }
    }
    public void incrementCounter(String label) {
        requestCounts.computeIfAbsent(label, k -> new AtomicLong(0)).incrementAndGet();
    }
    public Map<String, Object> snapshot() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("timestamp", Instant.now().toString());
        Map<String, Map<String, Object>> latencyReport = new LinkedHashMap<>();
        latencySamples.forEach((op, samples) -> {
            long[] data;
            synchronized (samples) {
                data = samples.stream().mapToLong(Long::longValue).toArray();
            }
            if (data.length == 0) return;
            Arrays.sort(data);
            latencyReport.put(op, Map.of(
                    "samples", data.length,
                    "minMs",   data[0],
                    "maxMs",   data[data.length - 1],
                    "avgMs",   Arrays.stream(data).average().orElse(0),
                    "p50Ms",   percentile(data, 50),
                    "p95Ms",   percentile(data, 95),
                    "p99Ms",   percentile(data, 99)
            ));
        });
        report.put("latency", latencyReport);
        Map<String, Long> counts = new LinkedHashMap<>();
        requestCounts.forEach((k, v) -> counts.put(k, v.get()));
        report.put("requestCounts", counts);
        long usedHeapMB = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxHeapMB = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long usedNonHeapMB = memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);
        report.put("memory", Map.of(
                "usedHeapMB",    usedHeapMB,
                "maxHeapMB",     maxHeapMB,
                "heapUsagePct",  maxHeapMB > 0 ? (usedHeapMB * 100 / maxHeapMB) : 0,
                "usedNonHeapMB", usedNonHeapMB
        ));
        double sysLoad = osBean.getSystemLoadAverage();
        report.put("system", Map.of("availableProcessors", Runtime.getRuntime().availableProcessors(), "systemLoadAverage",   sysLoad >= 0 ? sysLoad : "N/A"));
        return report;
    }
    private long percentile(long[] sortedData, int pct) {
        int idx = (int) Math.ceil(pct / 100.0 * sortedData.length) - 1;
        return sortedData[Math.max(0, Math.min(idx, sortedData.length - 1))];
    }
}