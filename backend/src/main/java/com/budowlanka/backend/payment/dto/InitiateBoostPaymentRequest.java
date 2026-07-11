package com.budowlanka.backend.payment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Żądanie inicjacji płatności za pakiet Boost.
 *
 * @param boostPackageId identyfikator pakietu z katalogu {@code boost_packages}
 */
public record InitiateBoostPaymentRequest(@NotNull UUID boostPackageId) {}
