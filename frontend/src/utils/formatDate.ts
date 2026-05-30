export function formatDate(isoString: string): string {
  const date = new Date(isoString)
  if (isNaN(date.getTime())) return ''
  const currentYear = new Date().getFullYear()
  const dateYear = date.getFullYear()

  return new Intl.DateTimeFormat('pl-PL', {
    day: 'numeric',
    month: 'long',
    ...(dateYear !== currentYear ? { year: 'numeric' } : {}),
  }).format(date)
}
