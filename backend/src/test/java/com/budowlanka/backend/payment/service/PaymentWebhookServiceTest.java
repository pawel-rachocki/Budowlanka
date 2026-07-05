package com.budowlanka.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.payment.client.Przelewy24Client;
import com.budowlanka.backend.payment.dto.P24VerifyRequest;
import com.budowlanka.backend.payment.dto.P24WebhookNotification;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.enums.PaymentType;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testy logiki biznesowej webhooka P24 ({@link PaymentWebhookService#process}). Weryfikacja podpisu
 * jest odpowiedzialnością controllera (patrz {@code WebhookControllerTest}) — tutaj skupiamy się na
 * idempotentności, zgodności kwoty, potwierdzeniu u P24 i aktywacji pakietu.
 */
@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private Przelewy24Client przelewy24Client;
  @Mock private SubscriptionActivationService subscriptionActivationService;

  private PaymentWebhookService service;

  private final UUID paymentId = UUID.randomUUID();
  private static final long ORDER_ID = 987654321L;

  @BeforeEach
  void setUp() {
    service =
        new PaymentWebhookService(
            paymentRepository, przelewy24Client, subscriptionActivationService);
  }

  /** Notyfikacja z kwotą 8900 gr (89.00 PLN) i podanym sessionId. */
  private P24WebhookNotification notification(String sessionId, int amount) {
    return new P24WebhookNotification(
        12345, 12345, sessionId, amount, amount, "PLN", ORDER_ID, 25, "platnosc", "sig");
  }

  private P24WebhookNotification notification(String sessionId) {
    return notification(sessionId, 8900);
  }

  /** Płatność PENDING na 89.00 PLN typu LISTING. */
  private Payment pendingPayment() {
    return Payment.builder()
        .id(paymentId)
        .amountPln(new BigDecimal("89.00"))
        .currency("PLN")
        .paymentProvider("Przelewy24")
        .paymentType(PaymentType.LISTING)
        .status(PaymentStatus.PENDING)
        .build();
  }

  @Test
  void should_skipProcessing_when_paymentNotFound() {
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

    service.process(notification(paymentId.toString()));

    verify(przelewy24Client, never()).verifyTransaction(any());
    verify(subscriptionActivationService, never()).activate(any());
  }

  @Test
  void should_skipProcessing_when_sessionIdNotValidUuid() {
    service.process(notification("nie-jest-uuid"));

    verify(paymentRepository, never()).findById(any());
    verify(przelewy24Client, never()).verifyTransaction(any());
    verify(subscriptionActivationService, never()).activate(any());
  }

  @Test
  void should_beIdempotent_when_paymentAlreadyCompleted() {
    Payment payment =
        Payment.builder()
            .id(paymentId)
            .amountPln(new BigDecimal("89.00"))
            .paymentType(PaymentType.LISTING)
            .status(PaymentStatus.COMPLETED)
            .build();
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

    service.process(notification(paymentId.toString()));

    // Podwójna notyfikacja: brak ponownej weryfikacji i aktywacji, status bez zmian.
    verify(przelewy24Client, never()).verifyTransaction(any());
    verify(subscriptionActivationService, never()).activate(any());
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
  }

  @Test
  void should_notActivate_when_amountMismatch() {
    Payment payment = pendingPayment();
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

    // Notyfikacja twierdzi 5000 gr, płatność opiewa na 8900 gr.
    service.process(notification(paymentId.toString(), 5000));

    verify(przelewy24Client, never()).verifyTransaction(any());
    verify(subscriptionActivationService, never()).activate(any());
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  void should_markFailed_when_verifyReturnsFalse() {
    Payment payment = pendingPayment();
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(przelewy24Client.verifyTransaction(any(P24VerifyRequest.class))).thenReturn(false);

    service.process(notification(paymentId.toString()));

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    verify(subscriptionActivationService, never()).activate(any());
  }

  @Test
  void should_activateAndMarkCompleted_when_verifyOk() {
    Payment payment = pendingPayment();
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(przelewy24Client.verifyTransaction(any(P24VerifyRequest.class))).thenReturn(true);

    service.process(notification(paymentId.toString()));

    verify(subscriptionActivationService).activate(payment);
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(payment.getProviderTxId()).isEqualTo(String.valueOf(ORDER_ID));
    assertThat(payment.getCompletedAt()).isNotNull();
  }
}
