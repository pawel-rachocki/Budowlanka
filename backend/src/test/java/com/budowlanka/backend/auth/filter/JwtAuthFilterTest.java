package com.budowlanka.backend.auth.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.service.JwtService;
import com.budowlanka.backend.auth.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  private static final String VALID_TOKEN = "valid.jwt.token";
  private static final String EMAIL = "user@example.com";

  @Mock private JwtService jwtService;
  @Mock private UserDetailsServiceImpl userDetailsService;
  @Mock private FilterChain filterChain;

  private JwtAuthFilter filter;
  private User user;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthFilter(jwtService, userDetailsService);
    user =
        User.builder()
            .email(EMAIL)
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(true)
            .build();
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_skipAuthentication_when_noAuthorizationHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void should_skipAuthentication_when_headerIsNotBearer() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void should_setAuthentication_when_validAccessToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.validateAccessToken(VALID_TOKEN)).thenReturn(true);
    when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(EMAIL);
    when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .isEqualTo(user);
  }

  @Test
  void should_notSetAuthentication_when_tokenValidationFails() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer invalid.token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.validateAccessToken("invalid.token")).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).extractUsername("invalid.token");
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void should_notSetAuthentication_when_extractUsernameFails() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.validateAccessToken(VALID_TOKEN)).thenReturn(true);
    when(jwtService.extractUsername(VALID_TOKEN))
        .thenThrow(new IllegalArgumentException("Invalid token"));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void should_returnTrue_shouldNotFilter_when_actuatorPath() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/actuator/health");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  void should_returnFalse_shouldNotFilter_when_apiPath() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/auth/logout");

    assertThat(filter.shouldNotFilter(request)).isFalse();
  }
}
