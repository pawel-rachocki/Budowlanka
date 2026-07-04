package com.budowlanka.backend.payment.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.budowlanka.backend.config.P24Properties;
import com.budowlanka.backend.payment.dto.P24WebhookNotification;
import org.junit.jupiter.api.Test;

class P24SignatureUtilTest {

  // Wektor zgodny z dokumentacją P24: JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES, SHA384 hex.
  private static final P24Properties PROPS =
      new P24Properties(
          true,
          "54918",
          "54918",
          "dd83c740769d8880",
          "secret-api-key",
          "https://sandbox.przelewy24.pl",
          "http://localhost:5173/platnosc/sukces",
          "http://localhost:8080/api/payments/webhook/p24");

  private final P24SignatureUtil util = new P24SignatureUtil(PROPS);

  @Test
  void should_compute_known_register_signature_vector() {
    // JSON:
    // {"sessionId":"test-session-123","merchantId":54918,"amount":100,"currency":"PLN","crc":"dd83c740769d8880"}
    String sign = util.signRegister("test-session-123", 100, "PLN");

    assertThat(sign)
        .isEqualTo(
            "032b21f4c6ce23d75b29b62fc482725e76e32ab597a9772e896f04480828311939c711fd023dfaaf776af7b9ca2893b5");
  }

  @Test
  void should_produce_lowercase_hex_sha384_length() {
    String sign = util.signVerify("test-session-123", 123456L, 100, "PLN");

    assertThat(sign).hasSize(96).matches("[0-9a-f]+");
  }

  @Test
  void should_accept_correctly_signed_webhook_notification() {
    P24WebhookNotification signed =
        util.buildSignedNotification("sess-1", 100, 100, "PLN", 555L, 25, "stmt-abc");

    assertThat(util.verifyWebhookSignature(signed)).isTrue();
  }

  @Test
  void should_reject_webhook_notification_with_tampered_signature() {
    P24WebhookNotification signed =
        util.buildSignedNotification("sess-1", 100, 100, "PLN", 555L, 25, "stmt-abc");
    P24WebhookNotification tampered =
        new P24WebhookNotification(
            signed.merchantId(),
            signed.posId(),
            signed.sessionId(),
            signed.amount() + 1, // zmieniona kwota → podpis nieaktualny
            signed.originAmount(),
            signed.currency(),
            signed.orderId(),
            signed.methodId(),
            signed.statement(),
            signed.sign());

    assertThat(util.verifyWebhookSignature(tampered)).isFalse();
  }

  @Test
  void should_reject_webhook_notification_with_null_signature() {
    P24WebhookNotification unsigned =
        new P24WebhookNotification(54918, 54918, "sess-1", 100, 100, "PLN", 555L, 25, "stmt", null);

    assertThat(util.verifyWebhookSignature(unsigned)).isFalse();
  }
}
