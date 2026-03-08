---
name: write-tests
description: "Pisanie testów jednostkowych i integracyjnych. Użyj po implementacji
  serwisu/controllera lub gdy trzeba pokryć istniejący kod testami."
---

## Zasady pisania testów

### Testy jednostkowe (serwisy)
- Framework: JUnit 5 + Mockito
- Mockuj WSZYSTKIE zależności (repozytoria, zewnętrzne serwisy)
- Testuj: happy path, walidację, edge cases, rzucane wyjątki
- Nazwy: `should_expectedResult_when_condition()`
- Arrange-Act-Assert pattern

### Testy integracyjne (krytyczne flow)
- @SpringBootTest + Testcontainers (PostgreSQL)
- Testuj: rejestracja → weryfikacja email, płatność → aktywacja subskrypcji,
  upload → moderacja → approve/reject
- Osobny profil: application-test.properties

### Frontend (Sprint 6)
- Vitest + React Testing Library
- Testuj: custom hooks (mockowane API), krytyczne formularze

Uruchom testy po napisaniu: `cd backend && mvn test`
