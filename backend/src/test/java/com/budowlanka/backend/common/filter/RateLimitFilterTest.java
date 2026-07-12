package com.budowlanka.backend.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.budowlanka.backend.config.RateLimitProperties;
import com.budowlanka.backend.config.RateLimitProperties.Limit;
import jakarta.servlet.FilterChain;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  private static final int LOGIN_CAPACITY = 3;
  private static final int REGISTER_CAPACITY = 2;
  private static final int REVIEWS_CAPACITY = 2;
  private static final int PROFILES_LIST_CAPACITY = 2;

  @Mock private FilterChain filterChain;

  private RateLimitFilter filter;

  @BeforeEach
  void setUp() {
    filter = new RateLimitFilter(properties(true));
  }

  private static RateLimitProperties properties(boolean enabled) {
    Duration minute = Duration.ofMinutes(1);
    return new RateLimitProperties(
        enabled,
        new Limit(LOGIN_CAPACITY, minute),
        new Limit(REGISTER_CAPACITY, minute),
        new Limit(REVIEWS_CAPACITY, minute),
        new Limit(PROFILES_LIST_CAPACITY, minute));
  }

  private static MockHttpServletRequest request(String method, String uri) {
    return new MockHttpServletRequest(method, uri);
  }

  @Test
  void should_allowRequests_when_underLimit() throws Exception {
    for (int i = 0; i < LOGIN_CAPACITY; i++) {
      MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilterInternal(request("POST", "/api/auth/login"), response, filterChain);
      assertThat(response.getStatus()).isEqualTo(200);
    }

    verify(filterChain, times(LOGIN_CAPACITY)).doFilter(any(), any());
  }

  @Test
  void should_setRemainingHeader_when_requestConsumed() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request("POST", "/api/auth/login"), response, filterChain);

    assertThat(response.getHeader("X-Rate-Limit-Remaining"))
        .isEqualTo(String.valueOf(LOGIN_CAPACITY - 1));
  }

  @Test
  void should_return429WithRetryAfterAndBody_when_loginLimitExceeded() throws Exception {
    for (int i = 0; i < LOGIN_CAPACITY; i++) {
      filter.doFilterInternal(
          request("POST", "/api/auth/login"), new MockHttpServletResponse(), filterChain);
    }

    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilterInternal(request("POST", "/api/auth/login"), response, filterChain);

    assertThat(response.getStatus()).isEqualTo(429);
    assertThat(response.getHeader("Retry-After")).isNotNull();
    assertThat(Long.parseLong(response.getHeader("Retry-After"))).isBetween(1L, 60L);
    assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
    assertThat(response.getContentAsString())
        .contains("\"status\":429")
        .contains("Zbyt wiele żądań");
    // chain was invoked only for the requests under the limit
    verify(filterChain, times(LOGIN_CAPACITY)).doFilter(any(), any());
  }

  @Test
  void should_trackBucketsIndependently_when_differentIps() throws Exception {
    for (int i = 0; i < LOGIN_CAPACITY; i++) {
      MockHttpServletRequest request = request("POST", "/api/auth/login");
      request.setRemoteAddr("10.0.0.1");
      filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);
    }

    MockHttpServletRequest exhaustedIpRequest = request("POST", "/api/auth/login");
    exhaustedIpRequest.setRemoteAddr("10.0.0.1");
    MockHttpServletResponse exhaustedIpResponse = new MockHttpServletResponse();
    filter.doFilterInternal(exhaustedIpRequest, exhaustedIpResponse, filterChain);

    MockHttpServletRequest freshIpRequest = request("POST", "/api/auth/login");
    freshIpRequest.setRemoteAddr("10.0.0.2");
    MockHttpServletResponse freshIpResponse = new MockHttpServletResponse();
    filter.doFilterInternal(freshIpRequest, freshIpResponse, filterChain);

    assertThat(exhaustedIpResponse.getStatus()).isEqualTo(429);
    assertThat(freshIpResponse.getStatus()).isEqualTo(200);
  }

  @Test
  void should_trackBucketsIndependently_when_differentRulesSameIp() throws Exception {
    for (int i = 0; i < REGISTER_CAPACITY; i++) {
      filter.doFilterInternal(
          request("POST", "/api/auth/register"), new MockHttpServletResponse(), filterChain);
    }

    MockHttpServletResponse registerResponse = new MockHttpServletResponse();
    filter.doFilterInternal(request("POST", "/api/auth/register"), registerResponse, filterChain);

    MockHttpServletResponse loginResponse = new MockHttpServletResponse();
    filter.doFilterInternal(request("POST", "/api/auth/login"), loginResponse, filterChain);

    assertThat(registerResponse.getStatus()).isEqualTo(429);
    assertThat(loginResponse.getStatus()).isEqualTo(200);
  }

  @Test
  void should_passThrough_when_pathNotMatched() throws Exception {
    for (int i = 0; i < LOGIN_CAPACITY + 2; i++) {
      MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilterInternal(request("POST", "/api/auth/refresh"), response, filterChain);
      assertThat(response.getStatus()).isEqualTo(200);
    }
  }

  @Test
  void should_passThrough_when_methodNotMatched() throws Exception {
    for (int i = 0; i < LOGIN_CAPACITY + 2; i++) {
      MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilterInternal(request("GET", "/api/auth/login"), response, filterChain);
      assertThat(response.getStatus()).isEqualTo(200);
    }
  }

  @Test
  void should_passThrough_when_optionsPreflight() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request("OPTIONS", "/api/auth/login"), response, filterChain);

    assertThat(response.getStatus()).isEqualTo(200);
    verify(filterChain).doFilter(any(), any());
  }

  @Test
  void should_limitReviews_when_postWithSlug() throws Exception {
    for (int i = 0; i < REVIEWS_CAPACITY; i++) {
      filter.doFilterInternal(
          request("POST", "/api/crew/profiles/ekipa-jana/reviews"),
          new MockHttpServletResponse(),
          filterChain);
    }

    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilterInternal(
        request("POST", "/api/crew/profiles/ekipa-jana/reviews"), response, filterChain);

    assertThat(response.getStatus()).isEqualTo(429);
  }

  @Test
  void should_limitProfilesListOnly_when_getProfiles() throws Exception {
    for (int i = 0; i < PROFILES_LIST_CAPACITY; i++) {
      filter.doFilterInternal(
          request("GET", "/api/crew/profiles"), new MockHttpServletResponse(), filterChain);
    }

    MockHttpServletResponse listResponse = new MockHttpServletResponse();
    filter.doFilterInternal(request("GET", "/api/crew/profiles"), listResponse, filterChain);

    MockHttpServletResponse slugResponse = new MockHttpServletResponse();
    filter.doFilterInternal(
        request("GET", "/api/crew/profiles/kowalski-remonty"), slugResponse, filterChain);

    assertThat(listResponse.getStatus()).isEqualTo(429);
    assertThat(slugResponse.getStatus()).isEqualTo(200);
  }

  @Test
  void should_returnTrue_shouldNotFilter_when_disabled() {
    RateLimitFilter disabledFilter = new RateLimitFilter(properties(false));

    assertThat(disabledFilter.shouldNotFilter(request("POST", "/api/auth/login"))).isTrue();
  }

  @Test
  void should_returnFalse_shouldNotFilter_when_enabled() {
    assertThat(filter.shouldNotFilter(request("POST", "/api/auth/login"))).isFalse();
  }
}
