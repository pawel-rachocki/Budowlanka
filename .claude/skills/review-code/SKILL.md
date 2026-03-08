---
name: review-code
description: "Przegląd kodu pod kątem jakości, bezpieczeństwa i best practices.
  Użyj po implementacji feature'a lub przed commitem."
---

Przeprowadź code review sprawdzając:

## Bezpieczeństwo
- Brak SQL injection (czy używamy parametryzowane query / Spring Data?)
- Brak XSS (czy walidujemy/escapujemy input?)
- Autoryzacja: czy endpointy sprawdzają role? Czy owner-only operacje sprawdzają ownership?
- Czy credentials nie są hardcoded?
- Czy walidacja DTO jest kompletna (Bean Validation)?

## Jakość kodu
- Czy Controller nie zawiera logiki biznesowej?
- Czy DTO są osobne od encji? (używamy Java records dla DTO)
- Czy serwisy używają constructor injection (nie @Autowired na polach)?
- Czy Optional zamiast null?
- Czy metody < 30 linii?
- Czy nazewnictwo jest spójne i opisowe?

## Performance
- Czy jest N+1 query problem? (brak @EntityGraph lub JOIN FETCH)
- Czy listy używają paginacji?
- Czy operacje async tam gdzie powinny (upload, moderacja)?

## Frontend
- Czy TypeScript strict, brak any?
- Czy loading/error states obsłużone?
- Czy responsywne (mobile-first)?

Format output: lista znalezionych problemów z severity (CRITICAL/WARNING/SUGGESTION)
i konkretną propozycją fix'a.
