package com.budowlanka.backend.payment.controller;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.payment.dto.InitiateBoostPaymentRequest;
import com.budowlanka.backend.payment.dto.InitiateListingPaymentRequest;
import com.budowlanka.backend.payment.dto.PaymentInitResponse;
import com.budowlanka.backend.payment.dto.PaymentResponse;
import com.budowlanka.backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inicjacja płatności ekipy za pakiet ogłoszenia i Boost oraz podgląd historii płatności. Cała
 * logika biznesowa (zapis PENDING, rejestracja w Przelewy24) jest w {@link PaymentService} — tu
 * tylko warstwa HTTP.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/listing")
  @PreAuthorize("hasRole('CREW')")
  public PaymentInitResponse initiateListing(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody InitiateListingPaymentRequest request) {
    return paymentService.initiateListingPayment(user, request.packageId());
  }

  @PostMapping("/boost")
  @PreAuthorize("hasRole('CREW')")
  public PaymentInitResponse initiateBoost(
      @AuthenticationPrincipal User user, @Valid @RequestBody InitiateBoostPaymentRequest request) {
    return paymentService.initiateBoostPayment(user, request.boostPackageId());
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('CREW')")
  public List<PaymentResponse> myPayments(@AuthenticationPrincipal User user) {
    return paymentService.getPaymentHistory(user);
  }
}
