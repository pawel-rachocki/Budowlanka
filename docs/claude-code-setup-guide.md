# Setup Claude Code — Portal Ekipy Remontowe

## Spis treści

1. [Filozofia: 4 warstwy konfiguracji](#1-filozofia-4-warstwy-konfiguracji)
2. [Struktura projektu (monorepo)](#2-struktura-projektu-monorepo)
3. [CLAUDE.md — Root (główny)](#3-claudemd--root)
4. [CLAUDE.md — Backend](#4-claudemd--backend)
5. [CLAUDE.md — Frontend](#5-claudemd--frontend)
6. [SPEC.md — Specyfikacja MVP](#6-specmd--specyfikacja-mvp)
7. [Skills — slash komendy](#7-skills--slash-komendy)
8. [Hooks — deterministyczna kontrola](#8-hooks--deterministyczna-kontrola)
9. [Subagenci (Agents)](#9-subagenci)
10. [settings.json](#10-settingsjson)
11. [Workflow dzienny](#11-workflow-dzienny)
12. [Zasady zarządzania kontekstem](#12-zasady-zarządzania-kontekstem)

---

## 1. Filozofia: 4 warstwy konfiguracji

Claude Code ma 4 mechanizmy rozszerzania. Każdy ma inną naturę:

| Warstwa | Plik/Folder | Natura | Kiedy się odpala |
|---------|-------------|--------|------------------|
| **CLAUDE.md** | `CLAUDE.md` | Probabilistyczna | Każda sesja — auto-load |
| **Skills** | `.claude/skills/*/SKILL.md` | Probabilistyczna | On-demand — Claude sam decyduje lub `/nazwa` |
| **Hooks** | `.claude/settings.json` | **Deterministyczna** | Zawsze — shell script przy evencie |
| **Agents** | `.claude/agents/*.md` | Probabilistyczna | Subagent z własnym context window |

**Złota zasada:** CLAUDE.md < 200 linii w root. Reszta → skills, agents, osobne docs. Im więcej instrukcji wrzucisz do CLAUDE.md, tym gorzej Claude je wszystkie realizuje.

---

## 2. Struktura projektu (monorepo)

```
ekipy-remontowe/
├── CLAUDE.md                          ← ROOT: ładowany zawsze
├── SPEC.md                            ← specyfikacja MVP (referencja)
├── .claude/
│   ├── settings.json                  ← hooks, permissions
│   ├── skills/
│   │   ├── implement-feature/
│   │   │   └── SKILL.md               ← /implement-feature
│   │   ├── review-code/
│   │   │   └── SKILL.md               ← /review-code
│   │   ├── write-tests/
│   │   │   └── SKILL.md               ← /write-tests
│   │   └── create-endpoint/
│   │       └── SKILL.md               ← /create-endpoint
│   └── agents/
│       ├── reviewer.md                ← subagent do code review
│       └── planner.md                 ← subagent do planowania
├── backend/
│   ├── CLAUDE.md                      ← backend-specific rules
│   ├── pom.xml
│   └── src/
├── frontend/
│   ├── CLAUDE.md                      ← frontend-specific rules
│   ├── package.json
│   └── src/
└── docs/
    ├── architecture.md                ← decyzje architektoniczne
    ├── database-schema.sql            ← schemat DB (referencja)
    └── api-contracts.md               ← kontrakty REST API
```

**Dlaczego monorepo?** Claude Code widzi cały kontekst: schemat DB, API, frontend — w jednym repozytorium. Może czytać schemat SQL pisząc endpoint, i czytać endpoint pisząc komponent React.

---

## 3. CLAUDE.md — Root (główny)

Zapisz jako `CLAUDE.md` w katalogu głównym projektu.

```markdown
# Portal Ekipy Remontowe

Marketplace łączący ekipy remontowe/budowlane z klientami w Polsce.
Monorepo: backend (Java/Spring Boot) + frontend (React/TypeScript).

## Architektura

- Backend: Java 21, Spring Boot 3.x, Spring Security + JWT, PostgreSQL 16
- Frontend: React 18+ z hooks, TypeScript strict, React Router, Axios
- Storage: S3-compatible (MinIO dev / Cloudflare R2 prod)
- Płatności: Przelewy24
- Moderacja zdjęć: SightEngine API (async)
- Migracje DB: Flyway

## Kluczowe komendy

- Backend build: `cd backend && ./mvnw clean verify`
- Backend run: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- Backend testy: `cd backend && ./mvnw test`
- Frontend install: `cd frontend && npm ci`
- Frontend dev: `cd frontend && npm run dev`
- Frontend build: `cd frontend && npm run build`
- Frontend lint: `cd frontend && npm run lint`
- Frontend typecheck: `cd frontend && npx tsc --noEmit`
- DB migration: `cd backend && ./mvnw flyway:migrate`

## Struktura kodu

- `backend/src/main/java/pl/ekipyremontowe/` — główny pakiet Java
- `backend/src/main/resources/db/migration/` — migracje Flyway (V1__, V2__...)
- `frontend/src/pages/` — strony (React Router)
- `frontend/src/components/` — komponenty wielokrotnego użytku
- `frontend/src/api/` — klient Axios + typy DTO
- `frontend/src/hooks/` — custom React hooks
- `docs/` — architektura, schemat DB, kontrakty API

## Konwencje kodu

- Język kodu: angielski (zmienne, klasy, komentarze). UI i komunikaty użytkownika: polski
- Każdy nowy endpoint: najpierw dodaj kontrakt do `docs/api-contracts.md`
- Każda zmiana schematu DB: migracja Flyway, nigdy ręczne ALTER
- Commity: Conventional Commits (feat:, fix:, refactor:, test:, docs:)
- Branch per feature: `feature/SPRINT-X-opis`, np. `feature/S2-crew-profiles`
- Testy: każdy serwis ma testy jednostkowe, krytyczne flow mają testy integracyjne (Testcontainers)

## Reguły bezpieczeństwa

- NIGDY nie commituj credentials, kluczy API, haseł. Wszystko przez env/application-dev.yml w .gitignore
- NIGDY git push --force na main
- NIGDY rm -rf bez potwierdzenia
- Hasła: BCrypt. JWT: access 15min, refresh 7d. Walidacja: Bean Validation na każdym DTO

## Specyfikacja i docs

- Pełna spec MVP: zobacz @SPEC.md
- Schemat bazy: zobacz @docs/database-schema.sql
- Kontrakty API: zobacz @docs/api-contracts.md
- Decyzje arch: zobacz @docs/architecture.md
```

**Uwaga:** To ~60 linii. Celowo zwięzłe. Szczegóły techniczne → pliki referencyjne (`@SPEC.md`, `@docs/...`). Claude je przeczyta gdy będzie potrzebował.

---

## 4. CLAUDE.md — Backend

Zapisz jako `backend/CLAUDE.md`.

```markdown
# Backend — Spring Boot

## Struktura pakietów

pl.ekipyremontowe/
├── config/          — Spring Security, CORS, S3, async config
├── auth/            — JWT, rejestracja, weryfikacja email
├── crew/            — profil ekipy, CRUD, wyszukiwanie
├── photo/           — upload, moderacja (SightEngine), S3 storage
├── review/          — opinie klientów
├── payment/         — integracja Przelewy24, webhooks, scheduled jobs
├── admin/           — panel admina, moderacja
├── common/          — wyjątki, DTO bazowe, utils
└── messaging/       — (placeholder na przyszły czat, nie implementuj teraz)

## Wzorce

- Warstwa: Controller → Service → Repository. Controller NIGDY nie zawiera logiki biznesowej.
- DTO: osobne klasy Request/Response w podpakiecie dto/. Nigdy nie zwracaj encji JPA bezpośrednio.
- Wyjątki: GlobalExceptionHandler z @RestControllerAdvice. Custom exceptions dziedziczą z RuntimeException.
- Paginacja: zawsze używaj Spring Data Pageable. Domyślny rozmiar strony: 20.

## Testy

- Testy jednostkowe: JUnit 5 + Mockito. Mockuj repozytoria w testach serwisów.
- Testy integracyjne: @SpringBootTest + Testcontainers (PostgreSQL).
- Nazewnictwo: `should_zwracaćOczekiwanyWynik_gdyWarunki()` lub camelCase angielski.
- Testy integracyjne w `src/test/java/.../integration/`.

## Styl kodu

- Final na polach serwisów (constructor injection, nie @Autowired na polach)
- Records dla DTO (Java records, nie klasy z getterami)
- Optional zamiast null. Nigdy nie zwracaj null z serwisów.
- Logowanie: SLF4J (@Slf4j lombok). Poziomy: ERROR dla błędów, WARN dla nietypowych sytuacji, INFO dla flow biznesowego, DEBUG dla szczegółów.
```

---

## 5. CLAUDE.md — Frontend

Zapisz jako `frontend/CLAUDE.md`.

```markdown
# Frontend — React + TypeScript

## Struktura

src/
├── api/             — klient Axios, interceptory, typy DTO
├── components/      — reusable komponenty (Button, Card, Rating, PhotoUpload...)
├── pages/           — strony routera (HomePage, CrewListPage, CrewProfilePage, LoginPage...)
├── hooks/           — custom hooks (useAuth, useCrews, usePhotos...)
├── context/         — AuthContext (JWT, user state)
├── types/           — współdzielone typy TypeScript
└── utils/           — helpery (formatDate, formatPrice, slugify...)

## Reguły

- Functional components + hooks. Żadnych class components.
- TypeScript strict mode. Żadnych `any`. Każdy props ma interfejs.
- Stylowanie: Tailwind CSS (klasy utility). Żadnych plików .css oprócz globals.
- Fetching: custom hooks opakowujące Axios. Nie fetch() bezpośrednio w komponentach.
- Formularze: React Hook Form + zod do walidacji.
- Stany ładowania i błędów: każdy hook zwraca { data, isLoading, error }.
- Responsywność: mobile-first. Ekipy remontowe używają telefonów.

## Wzorce plików

- Komponent: `ComponentName.tsx` (PascalCase)
- Hook: `useNazwa.ts` (camelCase z prefixem use)
- Typ/interfejs: `nazwa.types.ts`
- Strona: `NazwaPage.tsx`
```

---

## 6. SPEC.md — Specyfikacja MVP

Stwórz plik `SPEC.md` w root projektu. Zawiera skondensowaną wersję roadmapy. Claude odczyta go gdy napiszesz `@SPEC.md` lub gdy skill/prompt poprosi o kontekst.

```markdown
# SPEC: Portal Ekipy Remontowe — MVP

## Cel biznesowy
Marketplace łączący ekipy remontowe z klientami w Polsce. Monetyzacja: ekipy
płacą za wystawienie profilu (pakiety 7/14/30/365 dni) + opcja Boost (wyższe
pozycjonowanie).

## Role użytkowników
- GUEST: przeglądanie ekip, rejestracja
- CLIENT: wystawianie opinii, przeglądanie danych kontaktowych ekip
- CREW: profil, portfolio zdjęć, zakup pakietów/boost
- ADMIN: moderacja zdjęć, blokowanie profili, dashboard

## Sprinty MVP (6 × 2 tyg)

### Sprint 1: Fundament + Auth
- Spring Boot init, Flyway, Spring Security + JWT (access 15min / refresh 7d)
- Rejestracja z weryfikacją email (link aktywacyjny)
- React: strony Login/Register, AuthContext, protected routes

### Sprint 2: Profile ekip + wyszukiwanie
- CRUD crew_profiles, service_categories, crew_services
- Seed 15-20 kategorii usług
- Lista ekip z filtrami (miasto, województwo, kategoria), paginacja
- Profil publiczny ekipy (bez zdjęć/opinii — Sprint 3-4)

### Sprint 3: Portfolio + moderacja zdjęć
- S3 upload (MinIO dev), thumbnail generation
- SightEngine API async moderacja NSFW
- Panel admina: kolejka moderacji, approve/reject
- Frontend: drag&drop upload, galeria, status moderacji

### Sprint 4: Opinie + ranking
- CRUD reviews (1 per client per crew)
- avg_rating + review_count na crew_profiles
- Ranking: ORDER BY (active_boost DESC, avg_rating DESC, review_count DESC)

### Sprint 5: Płatności
- Przelewy24 integracja (sandbox → prod)
- Webhook handler z weryfikacją podpisu + idempotentność
- Aktywacja subskrypcji, scheduled job wygaszania
- UI: wybór pakietu, redirect do P24, strona sukcesu/błędu

### Sprint 6: Polish + launch prep
- Admin dashboard (statystyki)
- Rate limiting, CORS prod, structured logging
- SEO (meta, OG, sitemap), responsywność, landing page
- Testy integracyjne krytycznych flow
- RODO: regulamin, polityka prywatności, cookie consent

## Schemat DB (główne tabele)
users, crew_profiles, service_categories, crew_services, portfolio_photos,
reviews, listing_packages, boost_packages, crew_subscriptions, crew_boosts,
payments, conversations (placeholder), messages (placeholder)

## Kluczowe integracje
- Przelewy24: REST API, webhook, CRC32/SHA384 weryfikacja
- SightEngine: REST API, async via @Async, thresholds nudity/gore/weapon
- S3: MinIO (dev), Cloudflare R2 (prod)
- Mail: Spring Mail + Mailtrap (dev) / SMTP prod
```

---

## 7. Skills — slash komendy

### /implement-feature

`.claude/skills/implement-feature/SKILL.md`:

```markdown
---
name: implement-feature
description: "Implementacja nowego feature'a od specyfikacji po testy. Użyj gdy rozpoczynasz
  nowy task z SPEC.md lub gdy ktoś poda opis feature'a do zbudowania."
---

Workflow implementacji feature'a:

1. **Przeczytaj kontekst**: Otwórz @SPEC.md i znajdź odpowiedni sprint/task.
   Otwórz @docs/api-contracts.md i @docs/database-schema.sql jeśli dotyczy.

2. **Zaplanuj**: Przed napisaniem kodu, wypisz:
   - Jakie pliki trzeba stworzyć/zmodyfikować
   - Jakie migracje Flyway potrzebne
   - Jakie endpointy (metoda, URL, request/response DTO)
   - Jakie testy napisać

3. **Implementuj warstwami** (w tej kolejności):
   a. Migracja Flyway (jeśli zmiana DB)
   b. Entity JPA
   c. Repository
   d. Service (+ unit test)
   e. Controller (+ DTO request/response)
   f. Test integracyjny (jeśli krytyczny flow)
   g. Frontend: typy DTO → hook API → komponent/strona

4. **Zweryfikuj**: Po implementacji uruchom:
   - `cd backend && ./mvnw test`
   - `cd frontend && npx tsc --noEmit && npm run lint`

5. **Zaktualizuj docs**: Dodaj/zaktualizuj kontrakt w @docs/api-contracts.md
```

### /review-code

`.claude/skills/review-code/SKILL.md`:

```markdown
---
name: review-code
description: "Przegląd kodu pod kątem jakości, bezpieczeństwa i best practices.
  Użyj po implementacji feature'a lub przed commitem."
---

Przeprowadź code review sprawdzając:

## Bezpieczeństwo
- Brak SQL injection (czy używamy parametryzowane query?)
- Brak XSS (czy walidujemy/escapujemy input?)
- Autoryzacja: czy endpointy sprawdzają role? Czy owner-only operacje sprawdzają ownership?
- Czy credentials nie są hardcoded?
- Czy walidacja DTO jest kompletna (Bean Validation)?

## Jakość kodu
- Czy Controller nie zawiera logiki biznesowej?
- Czy DTO są osobne od encji?
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
```

### /write-tests

`.claude/skills/write-tests/SKILL.md`:

```markdown
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
- Nazwy: `should_[oczekiwany rezultat]_when_[warunek]`
- Arrange-Act-Assert pattern

### Testy integracyjne (krytyczne flow)
- @SpringBootTest + Testcontainers (PostgreSQL)
- Testuj: rejestracja → weryfikacja email, płatność → aktywacja subskrypcji,
  upload → moderacja → approve/reject
- Osobny profil: application-test.yml

### Frontend (opcjonalnie, Sprint 6)
- Vitest + React Testing Library
- Testuj: custom hooks (mockowane API), krytyczne formularze

Uruchom testy po napisaniu: `cd backend && ./mvnw test`
```

### /create-endpoint

`.claude/skills/create-endpoint/SKILL.md`:

```markdown
---
name: create-endpoint
description: "Tworzenie nowego REST API endpoint od DTO po controller. Użyj gdy
  potrzebujesz szybko dodać endpoint z pełną strukturą warstw."
---

Tworzenie endpointu — checklist:

1. **DTO** (w pakiecie `dto/` odpowiedniego modułu):
   - `XxxRequest` (record z @Valid, @NotBlank, @Size etc.)
   - `XxxResponse` (record, nigdy encja)

2. **Service**:
   - Metoda biznesowa, zwraca DTO response lub void
   - Rzuca custom exception przy błędach (np. ResourceNotFoundException)
   - Unit test w `src/test/java/`

3. **Controller**:
   - @RestController, @RequestMapping("/api/...")
   - @PreAuthorize dla autoryzacji roli
   - ResponseEntity z odpowiednim HTTP status (201 Created, 204 No Content etc.)
   - @Valid na @RequestBody

4. **Aktualizuj docs/api-contracts.md** z nowym endpointem

Nie zapomnij: jeśli endpoint wymaga nowej tabeli → najpierw migracja Flyway.
```

---

## 8. Hooks — deterministyczna kontrola

Hooks to shell skrypty, które odpalają się **zawsze** — w odróżnieniu od CLAUDE.md (probabilistyczne). Konfiguracja w `.claude/settings.json`.

### Dlaczego hooks, a nie CLAUDE.md?

Reguła z CLAUDE.md "nigdy nie rób rm -rf" jest respektowana ~70% czasu. Hook z `exit 2` blokuje to **100% czasu**. Dla reguł bezpieczeństwa — zawsze hooks.

### Kluczowe hooki dla Twojego projektu:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "ARGS=$(echo \"$CLAUDE_TOOL_INPUT\" | jq -r '.command // empty' 2>/dev/null); if echo \"$ARGS\" | grep -qE 'rm\\s+-rf|git\\s+push\\s+--force|git\\s+push.*-f\\s|drop\\s+table|drop\\s+database'; then echo 'BLOCKED: Dangerous command detected. Use safe alternatives.' >&2; exit 2; fi"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": "FILE=$(echo \"$CLAUDE_FILE_PATHS\" | head -1); if [[ \"$FILE\" == *.java ]]; then echo 'Java file modified — remember to run tests'; fi; if [[ \"$FILE\" == *.tsx || \"$FILE\" == *.ts ]]; then cd frontend 2>/dev/null && npx tsc --noEmit --skipLibCheck \"$FILE\" 2>&1 | tail -5 || true; fi"
          }
        ]
      }
    ]
  }
}
```

**Co robią te hooki:**

| Hook | Event | Działanie |
|------|-------|-----------|
| Blokada niebezpiecznych komend | PreToolUse (Bash) | Blokuje `rm -rf`, `git push --force`, `DROP TABLE` — exit 2 = hard stop |
| TypeScript check po edycji | PostToolUse (Write/Edit) | Po edycji pliku .ts/.tsx → automatyczny `tsc --noEmit` |

---

## 9. Subagenci

Subagent = Claude z **własnym context window**. Deleguj mu zadanie, dostaje świeży kontekst, zwraca wynik. Twój główny kontekst nie rośnie.

### Reviewer

`.claude/agents/reviewer.md`:

```markdown
---
name: reviewer
description: "Expert code reviewer. Runs after feature implementation to catch
  security, performance, and quality issues."
model: sonnet
---

You are a senior Java/Spring Boot code reviewer with security expertise.

Review the code changes for:
1. Security vulnerabilities (injection, auth bypass, data exposure)
2. Spring Boot anti-patterns (logic in controllers, entity exposure, field injection)
3. N+1 queries and missing pagination
4. Missing error handling and input validation
5. TypeScript strict mode violations on frontend

Output format:
- CRITICAL: must fix before merge
- WARNING: should fix soon
- SUGGESTION: nice to have

Be concise. No praise, only actionable feedback.
```

### Planner

`.claude/agents/planner.md`:

```markdown
---
name: planner
description: "Architectural planner for breaking down features into tasks.
  Use before starting a new sprint or complex feature."
model: sonnet
---

You are a software architect planning implementation of features for a
Spring Boot + React marketplace platform.

When given a feature description:
1. Break it into ordered tasks (max 5-8 per feature)
2. For each task specify: files to create/modify, dependencies on other tasks
3. Estimate complexity (S/M/L)
4. Identify risks and edge cases
5. Define acceptance criteria

Reference @SPEC.md and @docs/architecture.md for project context.
Output a markdown checklist that can be tracked.
```

---

## 10. settings.json

Pełny `.claude/settings.json`:

```json
{
  "permissions": {
    "allow": [
      "Bash(cd backend && ./mvnw *)",
      "Bash(cd frontend && npm *)",
      "Bash(cd frontend && npx *)",
      "Bash(git add *)",
      "Bash(git commit *)",
      "Bash(git checkout *)",
      "Bash(git branch *)",
      "Bash(git status*)",
      "Bash(git log*)",
      "Bash(git diff*)",
      "Bash(cat *)",
      "Bash(ls *)",
      "Bash(mkdir *)",
      "Bash(find *)",
      "Bash(grep *)",
      "Bash(docker compose *)",
      "Read",
      "Write",
      "Edit"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(git push --force*)",
      "Bash(git push -f*)",
      "Bash(sudo *)"
    ]
  },
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "ARGS=$(echo \"$CLAUDE_TOOL_INPUT\" | jq -r '.command // empty' 2>/dev/null); if echo \"$ARGS\" | grep -qE 'rm\\s+-rf|git\\s+push\\s+--force|git\\s+push.*-f\\s|drop\\s+table|drop\\s+database'; then echo 'BLOCKED: Dangerous command detected.' >&2; exit 2; fi"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": "FILE=$(echo \"$CLAUDE_FILE_PATHS\" | head -1 2>/dev/null); if [[ \"$FILE\" == *.tsx || \"$FILE\" == *.ts ]]; then cd frontend 2>/dev/null && npx tsc --noEmit --skipLibCheck 2>&1 | tail -5 || true; fi"
          }
        ]
      }
    ]
  }
}
```

---

## 11. Workflow dzienny

### Rozpoczęcie sesji (nowy feature)

```
1. git checkout main && git pull
2. git checkout -b feature/S2-crew-profiles
3. claude                                    ← startuj Claude Code
4. [W Claude Code]:
   > Shift+Tab (Plan Mode)
   > "Implementuję Sprint 2 task: CRUD crew_profiles.
   >  Przeczytaj @SPEC.md sekcję Sprint 2 i @docs/database-schema.sql.
   >  Zaplanuj implementację krok po kroku."
   > [Przejrzyj plan, popraw jeśli trzeba]
   > Shift+Tab (wyjdź z Plan Mode)
   > "Ok, implementuj wg planu"
```

### W trakcie pracy

```
- Po każdym feature: /review-code
- Gdy kontekst > 50%:  /compact z opisem co zostało do zrobienia
- Gdy zmieniasz task:   /clear (nowa sesja, czysty kontekst)
- Commit po każdym atomowym kawałku: "git commit zgodnie z Conventional Commits"
```

### Koniec sesji

```
- Sprawdź git status — wszystko zacommitowane?
- Jeśli feature gotowy: "Utwórz podsumowanie tego co zrobiliśmy w tej sesji"
  → Zapisz do docs/devlog.md (opcjonalne, ale przydatne)
```

### Cheat sheet komend Claude Code

| Komenda | Działanie |
|---------|-----------|
| `Shift+Tab` | Przełącz Plan Mode (read-only, bez edycji) |
| `Esc` | Przerwij bieżącą akcję Claude |
| `/compact` | Skompresuj kontekst (z opcjonalnym opisem) |
| `/clear` | Nowa sesja (czysty kontekst) |
| `/review-code` | Twój custom skill — code review |
| `/implement-feature` | Twój custom skill — implementacja |
| `/write-tests` | Twój custom skill — pisanie testów |
| `/create-endpoint` | Twój custom skill — nowy endpoint |
| `#` | Dodaj instrukcję do CLAUDE.md na bieżąco |
| `/model` | Zmień model (Opus 4.6 do planowania, Sonnet 4.5 do implementacji) |
| `/config` | Konfiguracja (thinking mode, output style) |

---

## 12. Zasady zarządzania kontekstem

To jest **najważniejsza sekcja**. Context management to różnica między frustracją a produktywnością.

### Zasada 1: Jedna sesja = jeden feature/task

Nie mieszaj. Nie rób auth + profiles + photos w jednej sesji. Claude traci kontekst i zaczyna zapominać wcześniejsze instrukcje.

### Zasada 2: /compact na 50%, /clear na nowym tasku

Okno kontekstu się zapełnia. Gdy widzisz ostrzeżenie (lub czujesz że Claude zaczyna "głupieć") — `/compact "kontynuuję implementację serwisu CrewProfileService, zostały testy"`. Gdy zmieniasz task — `/clear`.

### Zasada 3: Plan Mode przed implementacją

Shift+Tab → opisz co chcesz → przejrzyj plan → Shift+Tab → implementuj. To nie jest opcjonalne. Bez tego Claude robi "vibe coding" — może zadziała, ale będziesz się cofać.

### Zasada 4: Dokumenty referencyjne > inline context

Zamiast opisywać schemat DB w prompcie, napisz "przeczytaj @docs/database-schema.sql". Claude sam go załaduje. Twój prompt jest krótszy, kontekst czystszy.

### Zasada 5: Git branch = safety net

Zawsze nowy branch przed feature'em. Jeśli Claude pójdzie w złą stronę → `git stash` lub `git checkout -- .` i zacznij od nowa. To jest tańsze niż naprawianie.

### Zasada 6: Model do zadania

| Zadanie | Model |
|---------|-------|
| Planowanie architektury, złożone decyzje | Opus 4.6 |
| Implementacja kodu, rutynowe taski | Sonnet 4.5 |
| Szybkie pytania, proste zmiany | Sonnet 4.5 |

Opus jest mądrzejszy, ale wolniejszy i droższy. Sonnet jest szybszy i wystarczający do 80% pracy kodowej.

---

## Quick Start — co zrobić TERAZ

1. **Stwórz repo** z strukturą z sekcji 2
2. **Skopiuj CLAUDE.md** (root + backend + frontend) z sekcji 3-5
3. **Skopiuj SPEC.md** z sekcji 6
4. **Stwórz skills** (4 pliki SKILL.md) z sekcji 7
5. **Stwórz agents** (2 pliki .md) z sekcji 9
6. **Skopiuj settings.json** z sekcji 10
7. **Stwórz docs/** z pustymi plikami: `architecture.md`, `database-schema.sql`, `api-contracts.md`
8. **Wrzuć schemat SQL** z roadmapy do `docs/database-schema.sql`
9. `git init && git add -A && git commit -m "chore: project setup with Claude Code config"`
10. `claude` → `/init` → przejrzyj co Claude zasugeruje → popraw CLAUDE.md jeśli trzeba

**Gotowe. Możesz zaczynać Sprint 1.**
