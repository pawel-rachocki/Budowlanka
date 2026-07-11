package com.budowlanka.backend.config;

import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Konfiguracja integracji z Przelewy24 (REST API v1).
 *
 * <p>Wartości wyłącznie z env/property — sekrety (crc, apiKey) NIGDY w repo. Wzór: {@link
 * S3Properties} / {@link SightEngineProperties}. Gdy {@code enabled=false} klient działa w trybie
 * mock (lokalny dev bez realnych kluczy), analogicznie do {@code app.moderation.enabled}.
 *
 * @param merchantId identyfikator sprzedawcy (liczba, trzymana jako String z property)
 * @param posId identyfikator punktu sprzedaży; jednocześnie login do Basic Auth
 * @param crc klucz CRC — wyłącznie do liczenia podpisów SHA384 (nie do Basic Auth)
 * @param apiKey klucz API — hasło do Basic Auth (nie mylić z crc)
 * @param baseUrl bazowy adres API, np. https://sandbox.przelewy24.pl
 * @param urlReturn adres powrotu klienta po płatności (front)
 * @param urlStatus adres webhooka notyfikacji (musi być publicznie osiągalny dla P24)
 */
@ConfigurationProperties("app.payments")
@Validated
public record P24Properties(
    boolean enabled,
    String merchantId,
    String posId,
    String crc,
    String apiKey,
    @Pattern(regexp = "^$|https?://.+", message = "must be a valid http/https URL or empty")
        String baseUrl,
    String urlReturn,
    String urlStatus) {}
