import type { Voivodeship } from '../types/crew.types'

export const VOIVODESHIP_LABELS: Record<Voivodeship, string> = {
  DOLNOSLASKIE: 'Dolnośląskie',
  KUJAWSKO_POMORSKIE: 'Kujawsko-Pomorskie',
  LUBELSKIE: 'Lubelskie',
  LUBUSKIE: 'Lubuskie',
  LODZKIE: 'Łódzkie',
  MALOPOLSKIE: 'Małopolskie',
  MAZOWIECKIE: 'Mazowieckie',
  OPOLSKIE: 'Opolskie',
  PODKARPACKIE: 'Podkarpackie',
  PODLASKIE: 'Podlaskie',
  POMORSKIE: 'Pomorskie',
  SLASKIE: 'Śląskie',
  SWIETOKRZYSKIE: 'Świętokrzyskie',
  WARMINSKO_MAZURSKIE: 'Warmińsko-Mazurskie',
  WIELKOPOLSKIE: 'Wielkopolskie',
  ZACHODNIOPOMORSKIE: 'Zachodniopomorskie',
}

/**
 * Województwa posortowane alfabetycznie po polskiej nazwie wyświetlanej
 * (zgodnie z kolejnością w `VOIVODESHIP_LABELS`, a nie po kluczu enum).
 * Używane do iteracji w komponentach Select.
 */
export const VOIVODESHIPS_ORDERED: readonly Voivodeship[] = (
  Object.keys(VOIVODESHIP_LABELS) as Voivodeship[]
).sort((a, b) => VOIVODESHIP_LABELS[a].localeCompare(VOIVODESHIP_LABELS[b], 'pl'))
