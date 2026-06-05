package com.servecreative.WholeProject.securityConfig;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimitFilter(@Value("${app.rate-limit.requests-per-minute:60}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/users/login")
                || request.getRequestURI().startsWith("/api/driver/auth/login")
                || request.getRequestURI().startsWith("/api/admin/login")) {
            String key = request.getRemoteAddr() + request.getRequestURI();
            Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("{\"error\":\"Too many requests. Try again later.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
