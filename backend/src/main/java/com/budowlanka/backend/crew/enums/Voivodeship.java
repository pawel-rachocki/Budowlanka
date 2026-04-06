package com.budowlanka.backend.crew.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Voivodeship {
  DOLNOSLASKIE("dolnośląskie"),
  KUJAWSKO_POMORSKIE("kujawsko-pomorskie"),
  LUBELSKIE("lubelskie"),
  LUBUSKIE("lubuskie"),
  LODZKIE("łódzkie"),
  MALOPOLSKIE("małopolskie"),
  MAZOWIECKIE("mazowieckie"),
  OPOLSKIE("opolskie"),
  PODKARPACKIE("podkarpackie"),
  PODLASKIE("podlaskie"),
  POMORSKIE("pomorskie"),
  SLASKIE("śląskie"),
  SWIETOKRZYSKIE("świętokrzyskie"),
  WARMINSKO_MAZURSKIE("warmińsko-mazurskie"),
  WIELKOPOLSKIE("wielkopolskie"),
  ZACHODNIOPOMORSKIE("zachodniopomorskie");

  private final String displayName;
}
