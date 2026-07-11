package com.budowlanka.backend.payment.client;

import com.budowlanka.backend.config.P24Properties;
import com.budowlanka.backend.payment.dto.P24RegisterRequest;
import com.budowlanka.backend.payment.dto.P24RegisterResult;
import com.budowlanka.backend.payment.dto.P24VerifyRequest;
import com.budowlanka.backend.payment.exception.P24ClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Klient REST API Przelewy24 (v1) - rejestracja i weryfikacja transakcji.
 *
 * <p>Uwierzytelnianie: Basic Auth (login {@code posId}, haslo {@code apiKey} - nie CRC). Podpisy
 * liczy {@link P24SignatureUtil}. Gdy {@code app.payments.enabled=false} dziala w trybie mock (bez
 * realnych wywolan sieciowych), analogicznie do {@code SightEngineClient}.
 *
 * <p>Odpowiedz czytana jest jako {@code String} i parsowana wlasnym {@link ObjectMapper} (tree) -
 * unika to niezgodnosci konwertera HTTP z typem JsonNode oraz problemu z nieznanymi polami w
 * odpowiedzi P24.
 */
@Slf4j
@Component
public class Przelewy24Client {

  private final P24Properties props;
  private final P24SignatureUtil signatureUtil;
  private final RestClient restClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public Przelewy24Client(P24Properties props, P24SignatureUtil signatureUtil) {
    this.props = props;
    this.signatureUtil = signatureUtil;
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(10));
    factory.setReadTimeout(Duration.ofSeconds(15));
    this.restClient = RestClient.builder().baseUrl(props.baseUrl()).requestFactory(factory).build();
  }

  Przelewy24Client(P24Properties props, P24SignatureUtil signatureUtil, RestClient restClient) {
    this.props = props;
    this.signatureUtil = signatureUtil;
    this.restClient = restClient;
  }

  /**
   * Rejestruje transakcje (POST /api/v1/transaction/register) i zwraca token + redirectUrl. W
   * trybie mock zwraca token zastepczy bez wywolania P24.
   */
  public P24RegisterResult registerTransaction(P24RegisterRequest req) {
    if (!props.enabled()) {
      String token = "mock-" + req.sessionId();
      log.info("P24 disabled - mock register for sessionId={}", req.sessionId());
      return new P24RegisterResult(token, redirectUrl(token));
    }
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("merchantId", Integer.parseInt(props.merchantId()));
      body.put("posId", Integer.parseInt(props.posId()));
      body.put("sessionId", req.sessionId());
      body.put("amount", req.amount());
      body.put("currency", req.currency());
      body.put("description", req.description());
      body.put("email", req.email());
      body.put("country", "PL");
      body.put("language", "pl");
      body.put("urlReturn", props.urlReturn());
      body.put("urlStatus", props.urlStatus());
      body.put("sign", signatureUtil.signRegister(req.sessionId(), req.amount(), req.currency()));

      JsonNode root = exchange("POST", "/api/v1/transaction/register", body);
      String token = root == null ? null : root.path("data").path("token").asText(null);
      if (token == null || token.isBlank()) {
        throw new P24ClientException("Przelewy24 register: brak tokenu w odpowiedzi");
      }
      return new P24RegisterResult(token, redirectUrl(token));
    } catch (P24ClientException e) {
      throw e;
    } catch (Exception e) {
      log.warn(
          "P24 registerTransaction failed for sessionId={}: {}", req.sessionId(), e.getMessage());
      throw new P24ClientException("Błąd komunikacji z Przelewy24 (register)", e);
    }
  }

  /**
   * Weryfikuje transakcje (PUT /api/v1/transaction/verify). W trybie mock zwraca zawsze {@code
   * true}.
   *
   * @return {@code true} gdy P24 potwierdzi status "success"
   */
  public boolean verifyTransaction(P24VerifyRequest req) {
    if (!props.enabled()) {
      log.info("P24 disabled - mock verify (success) for sessionId={}", req.sessionId());
      return true;
    }
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("merchantId", Integer.parseInt(props.merchantId()));
      body.put("posId", Integer.parseInt(props.posId()));
      body.put("sessionId", req.sessionId());
      body.put("amount", req.amount());
      body.put("currency", req.currency());
      body.put("orderId", req.orderId());
      body.put(
          "sign",
          signatureUtil.signVerify(req.sessionId(), req.orderId(), req.amount(), req.currency()));

      JsonNode root = exchange("PUT", "/api/v1/transaction/verify", body);
      String status = root == null ? "" : root.path("data").path("status").asText("");
      return "success".equals(status);
    } catch (Exception e) {
      log.warn(
          "P24 verifyTransaction failed for sessionId={}: {}", req.sessionId(), e.getMessage());
      throw new P24ClientException("Błąd komunikacji z Przelewy24 (verify)", e);
    }
  }

  /** Pelny adres przekierowania klienta do bramki P24: {@code {baseUrl}/trnRequest/{token}}. */
  public String redirectUrl(String token) {
    return props.baseUrl() + "/trnRequest/" + token;
  }

  private JsonNode exchange(String method, String path, Map<String, Object> body) throws Exception {
    RestClient.RequestBodySpec spec =
        ("PUT".equals(method) ? restClient.put() : restClient.post())
            .uri(path)
            .headers(h -> h.setBasicAuth(props.posId(), props.apiKey()))
            .contentType(MediaType.APPLICATION_JSON);
    String json = spec.body(body).retrieve().body(String.class);
    return (json == null || json.isBlank()) ? null : objectMapper.readTree(json);
  }
}
