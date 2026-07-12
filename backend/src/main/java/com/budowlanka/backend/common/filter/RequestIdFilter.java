package com.budowlanka.backend.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Puts a request id into the MDC for log correlation and echoes it as {@code X-Request-Id} on the
 * response. Registered at the servlet level with highest precedence (unlike {@link
 * RateLimitFilter}) so logs emitted from the whole security chain — JWT auth, rate-limit 429s,
 * 401/403 handlers — carry the id too.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String HEADER_NAME = "X-Request-Id";
  public static final String MDC_KEY = "requestId";

  // Client-supplied ids are accepted only in a safe shape — anything else (log injection,
  // oversized garbage) is replaced with a generated UUID.
  private static final Pattern VALID_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{8,64}");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String requestId = resolveRequestId(request);
    MDC.put(MDC_KEY, requestId);
    response.setHeader(HEADER_NAME, requestId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  private static String resolveRequestId(HttpServletRequest request) {
    String incoming = request.getHeader(HEADER_NAME);
    if (incoming != null && VALID_REQUEST_ID.matcher(incoming).matches()) {
      return incoming;
    }
    return UUID.randomUUID().toString();
  }
}
