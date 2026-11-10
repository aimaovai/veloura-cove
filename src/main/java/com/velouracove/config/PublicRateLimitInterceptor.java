package com.velouracove.config;

import com.velouracove.common.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublicRateLimitInterceptor implements HandlerInterceptor {
    private static final long WINDOW_NANOS = 60_000_000_000L;
    private static final int REQUESTS_PER_WINDOW = 10;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (!isLimited(path)) return true;

        String group = path.contains("/payment-simulation") ? "payment"
                : path.equals("/api/v1/inquiries") ? "inquiry"
                : path.equals("/api/v1/reservation-holds") ? "hold" : "reservation";
        String key = request.getRemoteAddr() + ":" + group;
        long now = System.nanoTime();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(now));
        synchronized (bucket) {
            if (now - bucket.windowStarted >= WINDOW_NANOS) {
                bucket.windowStarted = now;
                bucket.requestCount = 0;
            }
            if (++bucket.requestCount > REQUESTS_PER_WINDOW) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED",
                        "Too many requests. Please wait before trying again.");
            }
        }
        if (buckets.size() > 10_000) {
            buckets.entrySet().removeIf(entry -> now - entry.getValue().windowStarted >= WINDOW_NANOS);
        }
        return true;
    }

    private boolean isLimited(String path) {
        return path.equals("/api/v1/inquiries")
                || path.equals("/api/v1/reservation-holds")
                || path.equals("/api/v1/reservations")
                || path.matches("/api/v1/reservations/[^/]+/payment-simulation");
    }

    private static final class Bucket {
        private volatile long windowStarted;
        private int requestCount;

        private Bucket(long windowStarted) {
            this.windowStarted = windowStarted;
        }
    }
}
