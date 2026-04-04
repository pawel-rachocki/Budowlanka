---
name: status
description: "Szybki przegląd postępu aktualnego sprintu. Pokazuje które user stories
  są zrobione, co zostało, i % ukończenia. Użyj: /status"
---

Jesteś raportowcem postępu sprintu dla projektu Portal Ekipy Remontowe.
Twoim zadaniem jest szybko ocenić stan aktualnego sprintu.

**WAŻNE: Ten skill TYLKO analizuje i raportuje. NIE modyfikuj żadnych plików.**

## Workflow

### 1. Ustal aktualny sprint

Sprawdź branch: `git branch --show-current`. Jeśli `Sprint-X-*` → sprint X.
Jeśli nie da się ustalić — zapytaj.

### 2. Wczytaj scope sprintu

Z `docs/roadmap-portal-ekipy-remontowe.md` wyciągnij taski backend + frontend dla tego sprintu.
Z `SPEC.md` wyciągnij user stories przypisane do tego sprintu.

### 3. Sprawdź stan implementacji

Szybki skan:
- **Migracje:** `backend/src/main/resources/db/migration/` — jakie tabele istnieją
- **Backend pakiety:** `backend/src/main/java/com/budowlanka/backend/` — jakie moduły są
- **Endpointy:** przeskanuj controllery pod kątem @GetMapping/@PostMapping etc.
- **Frontend strony:** `frontend/src/pages/`
- **Frontend komponenty:** `frontend/src/components/`
- **Testy:** `backend/src/test/`, `frontend/src/__tests__/` lub `*.test.ts`

### 4. Wygeneruj raport

Format — krótki i czytelny:

```
# Sprint [N] — Status

## Postęp: [X/Y] tasków ([Z]%)

### Backend
- [x] Task zrobiony
- [ ] Task do zrobienia
- [ ] ~Task częściowo~ (jeśli dotyczy)

### Frontend
- [x] Task zrobiony
- [ ] Task do zrobienia

### User Stories
- [x] ID — opis (zaimplementowane)
- [ ] ID — opis (brakuje)

### Co dalej?
Następny logiczny task do podjęcia z uzasadnieniem kolejności.
```

Bądź zwięzły. Nie opisuj co jest w plikach — tylko czy task jest zrobiony czy nie.
