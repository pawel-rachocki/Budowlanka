/**
 * Liczba pełnych dni od teraz do podanej daty (ISO). Zaokrąglenie w górę — okno kończące się
 * za pół dnia to wciąż „jeszcze 1 dzień". Zwraca 0 dla dat w przeszłości lub niepoprawnych.
 *
 * Używane w widgecie subskrypcji: od kiedy okno subskrypcji/boosta się „stackuje" (REM-164),
 * nazwa pakietu przestała odpowiadać realnemu czasowi — źródłem prawdy jest expiresAt.
 */
export function daysUntil(isoString: string): number {
  const target = new Date(isoString)
  if (isNaN(target.getTime())) return 0
  const diffMs = target.getTime() - Date.now()
  if (diffMs <= 0) return 0
  return Math.ceil(diffMs / (1000 * 60 * 60 * 24))
}

/** Polska odmiana słowa „dzień": 1 → „dzień", pozostałe liczby → „dni". */
export function dayLabel(days: number): string {
  return days === 1 ? 'dzień' : 'dni'
}
