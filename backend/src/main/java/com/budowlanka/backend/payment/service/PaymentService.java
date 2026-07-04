package com.budowlanka.backend.payment.service;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.client.Przelewy24Client;
import com.budowlanka.backend.payment.dto.P24RegisterRequest;
import com.budowlanka.backend.payment.dto.P24RegisterResult;
import com.budowlanka.backend.payment.dto.PaymentInitResponse;
import com.budowlanka.backend.payment.dto.PaymentResponse;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.ListingPackage;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentType;
import com.budowlanka.backend.payment.exception.PackageNotFoundException;
import com.budowlanka.backend.payment.mapper.PaymentMapper;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicjacja płatności za pakiet ogłoszenia (LISTING) i Boost (BOOST). Tworzy rekord {@code
 * payments} w stanie PENDING, rejestruje transakcję w Przelewy24 i zwraca adres przekierowania do
 * bramki.
 *
 * <p>Aktywacja subskrypcji/boosta następuje dopiero po zaksięgowaniu płatności — obsługuje to
 * webhook (B7) wraz z {@code SubscriptionActivationService} (B8), nie ten serwis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private static final String PROVIDER = "Przelewy24";
  private static final String CURRENCY = "PLN";

  private final CrewProfileRepository crewProfileRepository;
  private final ListingPackageRepository listingPackageRepository;
  private final BoostPackageRepository boostPackageRepository;
  private final PaymentRepository paymentRepository;
  private final Przelewy24Client przelewy24Client;
  private final PaymentMapper paymentMapper;

  @Transactional
  public PaymentInitResponse initiateListingPayment(User user, UUID packageId) {
    CrewProfile crew = requireCrewProfile(user);
    ListingPackage pkg =
        listingPackageRepository
            .findById(packageId)
            .filter(ListingPackage::isActive)
            .orElseThrow(PackageNotFoundException::new);

    String description = "Pakiet " + pkg.getName() + " – " + crew.getCompanyName();
    return initiate(crew, user, PaymentType.LISTING, packageId, pkg.getPricePln(), description);
  }

  @Transactional
  public PaymentInitResponse initiateBoostPayment(User user, UUID boostPackageId) {
    CrewProfile crew = requireCrewProfile(user);
    BoostPackage pkg =
        boostPackageRepository
            .findById(boostPackageId)
            .filter(BoostPackage::isActive)
            .orElseThrow(PackageNotFoundException::new);

    String description = "Boost " + pkg.getName() + " – " + crew.getCompanyName();
    return initiate(crew, user, PaymentType.BOOST, boostPackageId, pkg.getPricePln(), description);
  }

  @Transactional(readOnly = true)
  public List<PaymentResponse> getPaymentHistory(User user) {
    CrewProfile crew = requireCrewProfile(user);
    return paymentRepository.findByCrewProfileIdOrderByCreatedAtDesc(crew.getId()).stream()
        .map(paymentMapper::toResponse)
        .toList();
  }

  /**
   * Wspólny przebieg inicjacji: zapis PENDING (sessionId = payment.id), rejestracja w P24, zwrot
   * redirectUrl. Rejestracja P24 wykonywana jest w tej samej transakcji — błąd komunikacji wycofuje
   * osierocony rekord PENDING.
   */
  private PaymentInitResponse initiate(
      CrewProfile crew,
      User user,
      PaymentType type,
      UUID referenceId,
      BigDecimal pricePln,
      String description) {
    Payment payment =
        paymentRepository.saveAndFlush(
            Payment.builder()
                .crewProfile(crew)
                .amountPln(pricePln)
                .currency(CURRENCY)
                .paymentProvider(PROVIDER)
                .paymentType(type)
                .referenceId(referenceId)
                .build());

    P24RegisterResult result =
        przelewy24Client.registerTransaction(
            new P24RegisterRequest(
                payment.getId().toString(),
                toGrosze(pricePln),
                CURRENCY,
                description,
                user.getEmail()));

    log.info(
        "Zainicjowano płatność {} typu {} dla ekipy {} (kwota {} PLN)",
        payment.getId(),
        type,
        crew.getId(),
        pricePln);
    return new PaymentInitResponse(result.redirectUrl());
  }

  private CrewProfile requireCrewProfile(User user) {
    return crewProfileRepository
        .findByUserId(user.getId())
        .orElseThrow(CrewProfileNotFoundException::new);
  }

  /** Konwersja PLN → grosze (int), których wymaga API Przelewy24. */
  private static int toGrosze(BigDecimal pricePln) {
    return pricePln.movePointRight(2).intValueExact();
  }
}
