package com.budowlanka.backend.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public final class CookieUtils {

  public static final String REFRESH_COOKIE_NAME = "refresh_token";
  // Scoped to the refresh endpoint only — cookie is not sent to other paths
  private static final String REFRESH_COOKIE_PATH = "/api/auth/refresh";

  private CookieUtils() {}

  public static void setRefreshCookie(
      HttpServletResponse response, String token, long maxAgeSeconds, boolean secure) {
    ResponseCookie cookie =
        ResponseCookie.from(REFRESH_COOKIE_NAME, token)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path(REFRESH_COOKIE_PATH)
            .maxAge(maxAgeSeconds)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public static void clearRefreshCookie(HttpServletResponse response, boolean secure) {
    ResponseCookie cookie =
        ResponseCookie.from(REFRESH_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path(REFRESH_COOKIE_PATH)
            .maxAge(0)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
