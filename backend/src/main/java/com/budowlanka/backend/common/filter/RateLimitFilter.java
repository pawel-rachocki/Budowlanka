package com.budowlanka.backend.common.filter;

import com.budowlanka.backend.config.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

  // HttpServletResponse has no SC_ constant for 429
  private static final int TOO_MANY_REQUESTS = 429;
  private static final long MAX_TRACKED_BUCKETS = 50_000;

  private final RateLimitProperties properties;
  private final List<Rule> rules;
  private final Cache<String, Bucket> buckets;

  private record Rule(
      String name, HttpMethod method, PathPattern pattern, RateLimitProperties.Limit limit) {}

  public RateLimitFilter(RateLimitProperties properties) {
    this.properties = properties;
    PathPatternParser parser = new PathPatternParser();
    this.rules =
        List.of(
            new Rule("login", HttpMethod.POST, parser.parse("/api/auth/login"), properties.login()),
            new Rule(
                "register",
                HttpMethod.POST,
                parser.parse("/api/auth/register"),
                properties.register()),
            new Rule(
                "reviews",
                HttpMethod.POST,
                parser.parse("/api/crew/profiles/{slug}/reviews"),
                properties.reviews()),
            new Rule(
                "profiles-list",
                HttpMethod.GET,
                parser.parse("/api/crew/profiles"),
                properties.profilesList()));
    // Bounded cache prevents unbounded memory growth from IP scans on a long-running node
    this.buckets =
        Caffeine.newBuilder()
            .maximumSize(MAX_TRACKED_BUCKETS)
            .expireAfterAccess(Duration.ofHours(1))
            .build();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    Optional<Rule> matched = matchRule(request);
    if (matched.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }
    Rule rule = matched.get();

    // Behind a future reverse proxy getRemoteAddr() returns the proxy IP — configure
    // server.forward-headers-strategy=framework then; never parse X-Forwarded-For manually
    // (spoofable without a trusted proxy).
    String clientIp = request.getRemoteAddr();
    Bucket bucket = buckets.get(rule.name() + ":" + clientIp, key -> newBucket(rule.limit()));
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
      chain.doFilter(request, response);
      return;
    }

    long waitSeconds =
        Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
    log.warn("Rate limit exceeded rule={} ip={}", rule.name(), clientIp);
    response.setStatus(TOO_MANY_REQUESTS);
    response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(waitSeconds));
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    String body =
        String.format(
            "{\"status\":429,\"message\":\"Zbyt wiele żądań. Spróbuj ponownie później.\",\"timestamp\":\"%s\"}",
            Instant.now());
    response.getWriter().write(body);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !properties.enabled();
  }

  private Optional<Rule> matchRule(HttpServletRequest request) {
    PathContainer path = PathContainer.parsePath(request.getRequestURI());
    return rules.stream()
        .filter(rule -> rule.method().matches(request.getMethod()) && rule.pattern().matches(path))
        .findFirst();
  }

  private static Bucket newBucket(RateLimitProperties.Limit limit) {
    return Bucket.builder()
        .addLimit(
            bandwidth ->
                bandwidth
                    .capacity(limit.capacity())
                    .refillGreedy(limit.capacity(), limit.refillPeriod()))
        .build();
  }
}
