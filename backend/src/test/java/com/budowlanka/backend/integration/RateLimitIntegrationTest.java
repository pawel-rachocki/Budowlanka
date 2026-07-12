package com.budowlanka.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budowlanka.backend.IntegrationTestBase;
import com.budowlanka.backend.auth.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Rate limiting is disabled globally in test properties (buckets outlive test classes via the
 * shared Spring context); this test re-enables it in a dedicated context. Every test method uses
 * its own fake client IP because MockMvc defaults every request to 127.0.0.1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.rate-limit.enabled=true", "app.rate-limit.login.capacity=3"})
class RateLimitIntegrationTest extends IntegrationTestBase {

  private static final int LOGIN_CAPACITY = 3;
  private static final String LOGIN_BODY =
      "{\"email\":\"nonexistent@example.com\",\"password\":\"wrong-password\"}";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private EmailService emailService;

  private static MockHttpServletRequestBuilder loginFrom(String clientIp) {
    return post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(LOGIN_BODY)
        .with(
            request -> {
              request.setRemoteAddr(clientIp);
              return request;
            });
  }

  @Test
  void login_overLimit_returns429WithRetryAfterAndApiErrorBody() throws Exception {
    String ip = "10.99.0.1";

    for (int i = 0; i < LOGIN_CAPACITY; i++) {
      mockMvc.perform(loginFrom(ip)).andExpect(status().isUnauthorized());
    }

    mockMvc
        .perform(loginFrom(ip))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.message").value("Zbyt wiele żądań. Spróbuj ponownie później."));
  }

  @Test
  void login_differentIpAfterLimitExhausted_stillReturns401Not429() throws Exception {
    for (int i = 0; i <= LOGIN_CAPACITY; i++) {
      mockMvc.perform(loginFrom("10.99.1.1"));
    }

    mockMvc.perform(loginFrom("10.99.1.2")).andExpect(status().isUnauthorized());
  }

  @Test
  void login_overLimitWithOrigin_returns429WithCorsHeaders() throws Exception {
    String ip = "10.99.2.1";
    String origin = "http://localhost:5173";

    for (int i = 0; i < LOGIN_CAPACITY; i++) {
      mockMvc.perform(loginFrom(ip).header(HttpHeaders.ORIGIN, origin));
    }

    mockMvc
        .perform(loginFrom(ip).header(HttpHeaders.ORIGIN, origin))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
  }
}
