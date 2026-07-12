package com.budowlanka.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budowlanka.backend.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Allowed origins come from app.cors.allowed-origins (env-driven on prod); this test overrides the
 * property to prove the config is parameterized, not hardcoded to localhost.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = "app.cors.allowed-origins=https://allowed.example.com,https://second.example.com")
class CorsIntegrationTest extends IntegrationTestBase {

  private static final String ALLOWED_ORIGIN = "https://allowed.example.com";
  private static final String SECOND_ALLOWED_ORIGIN = "https://second.example.com";
  private static final String DISALLOWED_ORIGIN = "https://evil.example.com";

  @Autowired private MockMvc mockMvc;

  @Test
  void preflight_fromAllowedOrigin_returnsCorsHeaders() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
  }

  @Test
  void preflight_fromSecondConfiguredOrigin_returnsCorsHeaders() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, SECOND_ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, SECOND_ALLOWED_ORIGIN));
  }

  @Test
  void preflight_fromDisallowedOrigin_isRejectedWithoutCorsHeaders() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void simpleGet_fromAllowedOrigin_carriesAllowOriginHeader() throws Exception {
    mockMvc
        .perform(get("/api/categories").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
  }

  @Test
  void simpleGet_fromDisallowedOrigin_isRejected() throws Exception {
    mockMvc
        .perform(get("/api/categories").header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }
}
