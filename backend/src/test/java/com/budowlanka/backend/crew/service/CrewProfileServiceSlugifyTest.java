package com.budowlanka.backend.crew.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CrewProfileServiceSlugifyTest {

  @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
  @CsvSource({
    // podstawowe polskie litery z NFD (ogonek, kreska, kropka)
    "ą, a",
    "ę, e",
    "ó, o",
    "ś, s",
    "ź, z",
    "ż, z",
    "ć, c",
    "ń, n",
    // ł/Ł — nie dekompozycjonuje przez NFD, obsługiwane przez POLISH_CHARS
    "ł, l",
    "Ł, l",
    // wielkie litery
    "Ś, s",
    "Ź, z",
    "Ż, z",
    "Ć, c",
    "Ń, n",
    "Ą, a",
    "Ę, e",
    "Ó, o",
    // realne nazwy firm + miast
    "Łódź, lodz",
    "Żółw Remonty, zolw-remonty",
    "Świętosław Budowa, swietoslaw-budowa",
    "Ćma & Źródło, cma-zrodlo",
    "Śląsk Firma Sp. z o.o., slask-firma-sp-z-o-o",
    // znaki specjalne -> myślniki
    "ABC  XYZ, abc-xyz",
    "firma--remonty, firma-remonty",
    "-wiodaca-firma-, wiodaca-firma",
  })
  void should_slugify_correctly(String input, String expected) {
    assertThat(CrewProfileService.slugify(input)).isEqualTo(expected);
  }
}
