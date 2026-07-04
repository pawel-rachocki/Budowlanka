package com.budowlanka.backend.payment.client;

import com.budowlanka.backend.config.P24Properties;
import com.budowlanka.backend.payment.dto.P24WebhookNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Liczenie i weryfikacja podpisów SHA384 wymaganych przez Przelewy24 REST API v1.
 *
 * <p>P24 liczy podpis z {@code json_encode} z flagami {@code JSON_UNESCAPED_UNICODE |
 * JSON_UNESCAPED_SLASHES} i ze ściśle ustaloną kolejnością pól. Jackson 2.x domyślnie nie escapuje
 * ani slashy ({@code /}), ani Unicode, więc {@link ObjectMapper#writeValueAsString} na {@link
 * LinkedHashMap} (zachowuje kolejność wstawiania) daje dokładnie wymagany ciąg. Wynik podpisu to
 * lowercase hex z SHA384.
 *
 * <p>Pola merchantId/posId/crc pochodzą wyłącznie z {@link P24Properties}.
 */
@Component
public class P24SignatureUtil {

  private final P24Properties props;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public P24SignatureUtil(P24Properties props) {
    this.props = props;
  }

  /** Podpis rejestracji: {@code {sessionId, merchantId, amount, currency, crc}}. */
  public String signRegister(String sessionId, int amount, String currency) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("sessionId", sessionId);
    data.put("merchantId", merchantId());
    data.put("amount", amount);
    data.put("currency", currency);
    data.put("crc", props.crc());
    return sha384(serialize(data));
  }

  /** Podpis weryfikacji: {@code {sessionId, orderId, amount, currency, crc}}. */
  public String signVerify(String sessionId, long orderId, int amount, String currency) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("sessionId", sessionId);
    data.put("orderId", orderId);
    data.put("amount", amount);
    data.put("currency", currency);
    data.put("crc", props.crc());
    return sha384(serialize(data));
  }

  /**
   * Oczekiwany podpis notyfikacji webhooka: {@code {merchantId, posId, sessionId, amount,
   * originAmount, currency, orderId, methodId, statement, crc}}.
   */
  public String signWebhook(P24WebhookNotification n) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("merchantId", n.merchantId());
    data.put("posId", n.posId());
    data.put("sessionId", n.sessionId());
    data.put("amount", n.amount());
    data.put("originAmount", n.originAmount());
    data.put("currency", n.currency());
    data.put("orderId", n.orderId());
    data.put("methodId", n.methodId());
    data.put("statement", n.statement());
    data.put("crc", props.crc());
    return sha384(serialize(data));
  }

  /** Stałoczasowe porównanie podpisu z notyfikacji z podpisem wyliczonym lokalnie. */
  public boolean verifyWebhookSignature(P24WebhookNotification n) {
    if (n.sign() == null) {
      return false;
    }
    String expected = signWebhook(n);
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8), n.sign().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Dev-only: buduje poprawnie podpisaną notyfikację (sign liczony z CRC) — do ręcznego replay
   * webhooka w Postman/curl, gdy {@code app.payments.enabled=false}. Determinizm podpisu i
   * idempotentność webhooka testowane w T1 (REM-162).
   */
  public P24WebhookNotification buildSignedNotification(
      String sessionId,
      int amount,
      int originAmount,
      String currency,
      long orderId,
      int methodId,
      String statement) {
    P24WebhookNotification unsigned =
        new P24WebhookNotification(
            merchantId(),
            posId(),
            sessionId,
            amount,
            originAmount,
            currency,
            orderId,
            methodId,
            statement,
            null);
    return new P24WebhookNotification(
        unsigned.merchantId(),
        unsigned.posId(),
        unsigned.sessionId(),
        unsigned.amount(),
        unsigned.originAmount(),
        unsigned.currency(),
        unsigned.orderId(),
        unsigned.methodId(),
        unsigned.statement(),
        signWebhook(unsigned));
  }

  private int merchantId() {
    return Integer.parseInt(props.merchantId());
  }

  private int posId() {
    return Integer.parseInt(props.posId());
  }

  private String serialize(Map<String, Object> data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Nie udało się zserializować danych do podpisu P24", e);
    }
  }

  private String sha384(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-384");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        hex.append(Character.forDigit((b >> 4) & 0xF, 16));
        hex.append(Character.forDigit(b & 0xF, 16));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-384 niedostępny", e);
    }
  }
}
