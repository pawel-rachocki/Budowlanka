const priceFormatter = new Intl.NumberFormat('pl-PL', {
  style: 'currency',
  currency: 'PLN',
})

/** Formatuje kwotę w PLN wg polskiej lokalizacji, np. 89 → "89,00 zł". */
export function formatPrice(pln: number): string {
  return priceFormatter.format(pln)
}
