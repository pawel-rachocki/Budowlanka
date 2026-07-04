package com.budowlanka.backend.payment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Żądanie inicjacji płatności za pakiet ogłoszenia.
 *
 * @param packageId identyfikator pakietu z katalogu {@code listing_packages}
 */
public record InitiateListingPaymentRequest(@NotNull UUID packageId) {}
