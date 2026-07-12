package com.budowlanka.backend.common.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

  private final RequestIdFilter filter = new RequestIdFilter();

  @AfterEach
  void cleanUpMdc() {
    MDC.clear();
  }

  private static MockHttpServletRequest request(String headerValue) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/crew/profiles");
    if (headerValue != null) {
      request.addHeader(RequestIdFilter.HEADER_NAME, headerValue);
    }
    return request;
  }

  private static FilterChain capturingChain(AtomicReference<String> mdcDuringChain) {
    return (req, res) -> mdcDuringChain.set(MDC.get(RequestIdFilter.MDC_KEY));
  }

  @Test
  void should_generateUuid_when_headerMissing() throws Exception {
    AtomicReference<String> mdcDuringChain = new AtomicReference<>();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request(null), response, capturingChain(mdcDuringChain));

    String echoed = response.getHeader(RequestIdFilter.HEADER_NAME);
    assertThat(echoed).isNotNull();
    assertThat(UUID.fromString(echoed)).isNotNull();
    assertThat(mdcDuringChain.get()).isEqualTo(echoed);
  }

  @Test
  void should_propagateProvidedId_when_headerValid() throws Exception {
    String provided = "front-abc123.XYZ_009";
    AtomicReference<String> mdcDuringChain = new AtomicReference<>();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request(provided), response, capturingChain(mdcDuringChain));

    assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).isEqualTo(provided);
    assertThat(mdcDuringChain.get()).isEqualTo(provided);
  }

  @Test
  void should_generateNewId_when_headerContainsUnsafeCharacters() throws Exception {
    String injection = "abc123\ninjected-log-line";
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request(injection), response, (req, res) -> {});

    String echoed = response.getHeader(RequestIdFilter.HEADER_NAME);
    assertThat(echoed).isNotEqualTo(injection);
    assertThat(UUID.fromString(echoed)).isNotNull();
  }

  @Test
  void should_generateNewId_when_headerTooShort() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request("abc"), response, (req, res) -> {});

    assertThat(UUID.fromString(response.getHeader(RequestIdFilter.HEADER_NAME))).isNotNull();
  }

  @Test
  void should_generateNewId_when_headerTooLong() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request("a".repeat(65)), response, (req, res) -> {});

    assertThat(UUID.fromString(response.getHeader(RequestIdFilter.HEADER_NAME))).isNotNull();
  }

  @Test
  void should_clearMdc_when_requestCompletes() throws Exception {
    filter.doFilterInternal(request(null), new MockHttpServletResponse(), (req, res) -> {});

    assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
  }

  @Test
  void should_clearMdc_when_chainThrows() {
    MockHttpServletRequest request = request(null);
    MockHttpServletResponse response = new MockHttpServletResponse();

    try {
      filter.doFilterInternal(
          request,
          response,
          (req, res) -> {
            throw new IllegalStateException("boom");
          });
    } catch (Exception expected) {
      // exception propagates — MDC must still be cleaned
    }

    assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
  }
}
