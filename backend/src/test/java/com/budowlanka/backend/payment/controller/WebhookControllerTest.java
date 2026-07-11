package com.budowlanka.backend.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.payment.client.P24SignatureUtil;
import com.budowlanka.backend.payment.dto.P24WebhookNotification;
import com.budowlanka.backend.payment.service.PaymentWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class WebhookControllerTest {

  private final P24SignatureUtil signatureUtil = mock(P24SignatureUtil.class);
  private final PaymentWebhookService webhookService = mock(PaymentWebhookService.class);
  private final WebhookController controller = new WebhookController(signatureUtil, webhookService);

  private static P24WebhookNotification notification() {
    return new P24WebhookNotification(
        12345,
        12345,
        "3f1c0000-0000-0000-0000-000000000000",
        8900,
        8900,
        "PLN",
        987654321L,
        25,
        "platnosc",
        "sig");
  }

  @Test
  void should_return400AndSkipProcessing_when_signatureInvalid() {
    P24WebhookNotification n = notification();
    when(signatureUtil.verifyWebhookSignature(n)).thenReturn(false);

    ResponseEntity<Void> result = controller.handleP24(n);

    assertThat(result.getStatusCode().value()).isEqualTo(400);
    verify(webhookService, never()).process(n);
  }

  @Test
  void should_return200_when_signatureValidAndProcessedOk() {
    P24WebhookNotification n = notification();
    when(signatureUtil.verifyWebhookSignature(n)).thenReturn(true);

    ResponseEntity<Void> result = controller.handleP24(n);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(webhookService).process(n);
  }

  @Test
  void should_return200_when_businessErrorDuringProcessing() {
    P24WebhookNotification n = notification();
    when(signatureUtil.verifyWebhookSignature(n)).thenReturn(true);
    doThrow(new RuntimeException("verify failed")).when(webhookService).process(n);

    ResponseEntity<Void> result = controller.handleP24(n);

    // Po przejściu podpisu P24 zawsze dostaje 200 — inaczej ponawia notyfikację w nieskończoność.
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
