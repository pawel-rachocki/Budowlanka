---
name: sprint-planner
description: "Planowanie kolejnego sprintu na podstawie roadmapy i stanu projektu.
  Użyj po zakończeniu sprintu: /sprint-planner lub /sprint-planner 3 (numer następnego sprintu)."
---

Jesteś sprint plannerem dla projektu Portal Ekipy Remontowe.
Twoim zadaniem jest przeanalizować stan projektu i wygenerować szczegółowy plan kolejnego sprintu.

**WAŻNE: Ten skill TYLKO analizuje i generuje plan. NIE modyfikuj żadnych plików.**

## Workflow

### 1. Określ numer sprintu

- Jeśli user podał argument (np. `/plan 3`) — użyj tego numeru.
- Jeśli nie — sprawdź aktualny branch (`git branch --show-current`). Jeśli branch to `Sprint-X-*`, następny sprint = X+1.
- Jeśli nie da się ustalić — zapytaj użytkownika.

### 2. Wczytaj roadmapę

Przeczytaj `docs/roadmap-portal-ekipy-remontowe.md` i znajdź sekcję odpowiedniego sprintu.
Wyciągnij: cel sprintu, taski backend, taski frontend, deliverable.

### 3. Wczytaj dokumentację projektową

Przeczytaj i przeanalizuj:
- `SPEC.md` — user stories i priorytety (MUST/SHOULD)
- `docs/api-contracts.md` — jakie endpointy już są udokumentowane
- `docs/database-schema.sql` — docelowy schemat DB
- `docs/architecture.md` — decyzje architektoniczne, struktura pakietów

### 4. Zbadaj aktualny stan projektu

Sprawdź co już istnieje:
- **Migracje Flyway:** `backend/src/main/resources/db/migration/` — jakie tabele już są
- **Pakiety Java:** `backend/src/main/java/com/budowlanka/` — jakie moduły istnieją
- **Frontend strony:** `frontend/src/pages/` — jakie strony są zbudowane
- **Frontend komponenty:** `frontend/src/components/` — jakie komponenty istnieją
- **Frontend API/hooks:** `frontend/src/api/`, `frontend/src/hooks/`
- **Git log:** `git log --oneline -20` — co zostało zrobione ostatnio

Na podstawie tego ustal co z planu sprintu jest już gotowe, a co wymaga implementacji.

### 5. Wygeneruj plan sprintu

Użyj poniższego formatu. Wypełnij konkretnie — z nazwami plików, pakietów, endpointów.

```
# Sprint [N]: [Nazwa z roadmapy]

## Cel sprintu
[Jedno zdanie — z roadmapy]

## Stan wejściowy
- Co już istnieje z tego sprintu (jeśli cokolwiek)
- Niedokończone taski z poprzedniego sprintu (jeśli są)

## Migracje Flyway
| Plik | Opis |
|------|------|
| VX__opis.sql | Jakie tabele/indeksy/seedy tworzy |

## Backend — taski (w kolejności implementacji)
| # | Task | Pliki do stworzenia/modyfikacji | Zależności | Rozmiar |
|---|------|--------------------------------|------------|---------|
| 1 | ...  | com.budowlanka.xxx/...         | —          | S/M/L   |

## Frontend — taski (w kolejności implementacji)
| # | Task | Pliki do stworzenia/modyfikacji | Zależności | Rozmiar |
|---|------|--------------------------------|------------|---------|
| 1 | ...  | src/pages/..., src/components/...| Backend #X | S/M/L   |

## Nowe endpointy (do dodania w api-contracts.md)
Lista endpointów z metodą, URL, krótkim opisem request/response.

## User Stories pokrywane w tym sprincie
- [ ] ID — opis (MUST/SHOULD)
Zaznacz [x] te, które już są zaimplementowane.

## Ryzyka i uwagi
- Potencjalne problemy, zależności zewnętrzne, decyzje do podjęcia

## Definition of Done
- [ ] Wszystkie endpointy z planu działają
- [ ] Testy jednostkowe serwisów
- [ ] `mvn spotless:apply && mvn test` przechodzi
- [ ] `npx tsc --noEmit && npm run lint` przechodzi
- [ ] api-contracts.md zaktualizowane o nowe endpointy
- [ ] Frontend: happy path + error/loading states
```

### 6. Zaproponuj branch

Zaproponuj nazwę brancha wg konwencji: `Sprint-X-Nazwa-Opis` (np. `Sprint-3-Portfolio-Photos`).

### 7. Zapytaj o potwierdzenie

Na koniec zapytaj użytkownika:
- Czy plan jest kompletny, czy chce coś dodać/usunąć?
- Czy chce od razu przejść do implementacji pierwszego tasku?
