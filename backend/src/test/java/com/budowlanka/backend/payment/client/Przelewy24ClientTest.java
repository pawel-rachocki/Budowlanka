package com.budowlanka.backend.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.budowlanka.backend.config.P24Properties;
import com.budowlanka.backend.payment.dto.P24RegisterRequest;
import com.budowlanka.backend.payment.dto.P24RegisterResult;
import com.budowlanka.backend.payment.dto.P24VerifyRequest;
import com.budowlanka.backend.payment.exception.P24ClientException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class Przelewy24ClientTest {

  private static final P24Properties DISABLED =
      new P24Properties(
          false, "", "", "", "", "https://sandbox.przelewy24.pl", "http://ret", "http://status");

  private static final P24Properties ENABLED =
      new P24Properties(
          true,
          "54918",
          "54918",
          "dd83c740769d8880",
          "secret-api-key",
          "https://sandbox.przelewy24.pl",
          "http://ret",
          "http://status");

  private final P24SignatureUtil enabledSignatureUtil = new P24SignatureUtil(ENABLED);

  private static final P24RegisterRequest REGISTER_REQ =
      new P24RegisterRequest("sess-1", 100, "PLN", "Pakiet 30 dni", "ekipa@example.com");

  @Test
  void should_return_mock_token_when_payments_disabled() {
    var client = new Przelewy24Client(DISABLED, new P24SignatureUtil(DISABLED));

    P24RegisterResult result = client.registerTransaction(REGISTER_REQ);

    assertThat(result.token()).isEqualTo("mock-sess-1");
    assertThat(result.redirectUrl())
        .isEqualTo("https://sandbox.przelewy24.pl/trnRequest/mock-sess-1");
  }

  @Test
  void should_return_success_on_verify_when_payments_disabled() {
    var client = new Przelewy24Client(DISABLED, new P24SignatureUtil(DISABLED));

    boolean verified = client.verifyTransaction(new P24VerifyRequest("sess-1", 100, "PLN", 555L));

    assertThat(verified).isTrue();
  }

  @Test
  void should_parse_token_from_register_response_when_enabled() {
    RestClient.Builder builder = RestClient.builder().baseUrl(ENABLED.baseUrl());
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    var client = new Przelewy24Client(ENABLED, enabledSignatureUtil, builder.build());

    String expectedAuth =
        "Basic "
            + Base64.getEncoder()
                .encodeToString("54918:secret-api-key".getBytes(StandardCharsets.UTF_8));
    server
        .expect(requestTo("https://sandbox.przelewy24.pl/api/v1/transaction/register"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.AUTHORIZATION, expectedAuth))
        .andRespond(
            withSuccess("{\"data\": {\"token\": \"abc-token-123\"}}", MediaType.APPLICATION_JSON));

    P24RegisterResult result = client.registerTransaction(REGISTER_REQ);

    assertThat(result.token()).isEqualTo("abc-token-123");
    assertThat(result.redirectUrl())
        .isEqualTo("https://sandbox.przelewy24.pl/trnRequest/abc-token-123");
    server.verify();
  }

  @Test
  void should_throw_when_register_response_has_no_token() {
    RestClient.Builder builder = RestClient.builder().baseUrl(ENABLED.baseUrl());
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    var client = new Przelewy24Client(ENABLED, enabledSignatureUtil, builder.build());

    server
        .expect(requestTo("https://sandbox.przelewy24.pl/api/v1/transaction/register"))
        .andRespond(withSuccess("{\"data\": {}}", MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> client.registerTransaction(REGISTER_REQ))
        .isInstanceOf(P24ClientException.class)
        .hasMessageContaining("brak tokenu");
  }

  @Test
  void should_return_true_when_verify_status_success_and_enabled() {
    RestClient.Builder builder = RestClient.builder().baseUrl(ENABLED.baseUrl());
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    var client = new Przelewy24Client(ENABLED, enabledSignatureUtil, builder.build());

    server
        .expect(requestTo("https://sandbox.przelewy24.pl/api/v1/transaction/verify"))
        .andExpect(method(HttpMethod.PUT))
        .andRespond(
            withSuccess("{\"data\": {\"status\": \"success\"}}", MediaType.APPLICATION_JSON));

    boolean verified = client.verifyTransaction(new P24VerifyRequest("sess-1", 100, "PLN", 555L));

    assertThat(verified).isTrue();
    server.verify();
  }

  @Test
  void should_wrap_network_error_in_p24_client_exception() {
    RestClient.Builder builder = RestClient.builder().baseUrl(ENABLED.baseUrl());
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    var client = new Przelewy24Client(ENABLED, enabledSignatureUtil, builder.build());

    server
        .expect(requestTo("https://sandbox.przelewy24.pl/api/v1/transaction/register"))
        .andRespond(withServerError());

    assertThatThrownBy(() -> client.registerTransaction(REGISTER_REQ))
        .isInstanceOf(P24ClientException.class)
        .hasMessageContaining("register");
  }
}
