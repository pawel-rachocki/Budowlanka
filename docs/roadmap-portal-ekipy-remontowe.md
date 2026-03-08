# Roadmapa End-to-End: Portal Ekipy Remontowe × Klienci

## Spis treści

1. [Faza 0 — Architektura i Decyzje Techniczne](#faza-0)
2. [Faza 1 — Definicja i Zakres MVP](#faza-1)
3. [Faza 1.5 — Plan Deweloperski (Sprinty)](#faza-15)
4. [Faza 2 — Launch & Przygotowanie na Skalowanie](#faza-2)

---

## Faza 0 — Architektura i Decyzje Techniczne

### 0.1 Schemat bazy danych PostgreSQL (MVP)

Poniższy schemat obejmuje wszystkie byty potrzebne do MVP — od użytkowników, przez profile ekip, po pakiety płatności i moderację.

```sql
-- ============================================================
-- UŻYTKOWNICY I AUTORYZACJA
-- ============================================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENT', 'CREW', 'ADMIN')),
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(128),
    token_expires_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PROFILE EKIP REMONTOWYCH
-- ============================================================

CREATE TABLE crew_profiles (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE REFERENCES users(id),
    company_name    VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,        -- SEO-friendly URL
    description     TEXT,
    phone           VARCHAR(20),
    contact_email   VARCHAR(255),
    city            VARCHAR(100) NOT NULL,
    voivodeship     VARCHAR(50)  NOT NULL,               -- województwo
    service_radius_km INT        DEFAULT 50,
    nip             VARCHAR(10),                          -- opcjonalny NIP
    is_visible      BOOLEAN      NOT NULL DEFAULT FALSE,  -- widoczny dopiero po aktywnym pakiecie
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crew_city ON crew_profiles(city);
CREATE INDEX idx_crew_voivodeship ON crew_profiles(voivodeship);

-- ============================================================
-- SPECJALIZACJE / KATEGORIE USŁUG
-- ============================================================

CREATE TABLE service_categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,   -- np. "Hydraulika", "Elektryka", "Wykończenia"
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE crew_services (
    crew_profile_id BIGINT  NOT NULL REFERENCES crew_profiles(id) ON DELETE CASCADE,
    category_id     INT     NOT NULL REFERENCES service_categories(id),
    PRIMARY KEY (crew_profile_id, category_id)
);

-- ============================================================
-- PORTFOLIO ZDJĘĆ + MODERACJA
-- ============================================================

CREATE TABLE portfolio_photos (
    id              BIGSERIAL PRIMARY KEY,
    crew_profile_id BIGINT       NOT NULL REFERENCES crew_profiles(id) ON DELETE CASCADE,
    storage_key     VARCHAR(512) NOT NULL,   -- klucz w S3/MinIO
    thumbnail_key   VARCHAR(512),
    caption         VARCHAR(255),
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    moderation_note   TEXT,                  -- notatka admina przy odrzuceniu
    uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_photos_crew ON portfolio_photos(crew_profile_id);
CREATE INDEX idx_photos_moderation ON portfolio_photos(moderation_status);

-- ============================================================
-- OPINIE / RECENZJE
-- ============================================================

CREATE TABLE reviews (
    id              BIGSERIAL PRIMARY KEY,
    crew_profile_id BIGINT   NOT NULL REFERENCES crew_profiles(id),
    author_user_id  BIGINT   NOT NULL REFERENCES users(id),
    rating          SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(crew_profile_id, author_user_id)  -- 1 opinia per klient per ekipa
);

CREATE INDEX idx_reviews_crew ON reviews(crew_profile_id);

-- ============================================================
-- PAKIETY I PŁATNOŚCI (MONETYZACJA)
-- ============================================================

CREATE TABLE listing_packages (
    id             SERIAL PRIMARY KEY,
    name           VARCHAR(50)    NOT NULL,      -- "7 dni", "14 dni", "30 dni", "365 dni"
    duration_days  INT            NOT NULL,
    price_pln      NUMERIC(10,2)  NOT NULL,      -- cena brutto w PLN
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE boost_packages (
    id             SERIAL PRIMARY KEY,
    name           VARCHAR(50)    NOT NULL,      -- "Boost 7 dni", "Boost 30 dni"
    duration_days  INT            NOT NULL,
    price_pln      NUMERIC(10,2)  NOT NULL,
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE crew_subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    crew_profile_id BIGINT       NOT NULL REFERENCES crew_profiles(id),
    package_id      INT          NOT NULL REFERENCES listing_packages(id),
    starts_at       TIMESTAMPTZ  NOT NULL,
    expires_at      TIMESTAMPTZ  NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subs_crew ON crew_subscriptions(crew_profile_id);
CREATE INDEX idx_subs_expires ON crew_subscriptions(expires_at);

CREATE TABLE crew_boosts (
    id              BIGSERIAL PRIMARY KEY,
    crew_profile_id BIGINT       NOT NULL REFERENCES crew_profiles(id),
    boost_package_id INT         NOT NULL REFERENCES boost_packages(id),
    starts_at       TIMESTAMPTZ  NOT NULL,
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    crew_profile_id BIGINT       NOT NULL REFERENCES crew_profiles(id),
    amount_pln      NUMERIC(10,2) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'PLN',
    payment_provider VARCHAR(30) NOT NULL,       -- 'PRZELEWY24', 'STRIPE', 'TPAY'
    provider_tx_id  VARCHAR(255),                -- ID transakcji po stronie providera
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    payment_type    VARCHAR(20)  NOT NULL CHECK (payment_type IN ('LISTING', 'BOOST')),
    reference_id    BIGINT,                      -- crew_subscriptions.id lub crew_boosts.id
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);

-- ============================================================
-- PRZYGOTOWANIE POD PRZYSZŁY CZAT (Faza 2+)
-- ============================================================

CREATE TABLE conversations (
    id              BIGSERIAL PRIMARY KEY,
    client_user_id  BIGINT NOT NULL REFERENCES users(id),
    crew_user_id    BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(client_user_id, crew_user_id)
);

CREATE TABLE messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT       NOT NULL REFERENCES conversations(id),
    sender_user_id  BIGINT       NOT NULL REFERENCES users(id),
    content         TEXT         NOT NULL,
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conv ON messages(conversation_id, created_at);
```

**Kluczowe decyzje projektowe:**

| Decyzja | Uzasadnienie |
|---|---|
| `role` w tabeli `users` (nie osobne tabele) | Dla MVP wystarczy; RBAC po stronie Spring Security. Przy skali → osobna tabela `roles` + M:N. |
| `slug` w `crew_profiles` | SEO — profil ekipy dostępny pod `/ekipy/kowalski-remonty-warszawa`. |
| `is_visible` sterowany automatycznie | Profil widoczny = aktywny `crew_subscriptions` z `expires_at > NOW()`. Cron job lub query-time check. |
| Osobne tabele `listing_packages` / `boost_packages` | Różne produkty, różne ceny, niezależne cykle życia. |
| `conversations` + `messages` od razu | Tabele istnieją od dnia 1, ale API do czatu budujesz w Fazie 2. Zero migracji strukturalnych później. |

---

### 0.2 Architektura uploadu i moderacji zdjęć

#### Flow (diagram sekwencji)

```
Ekipa (Browser)          Spring Boot API           S3/MinIO          Moderacja NSFW
     |                        |                       |                    |
     |-- POST /api/photos --->|                       |                    |
     |   (multipart/form)     |                       |                    |
     |                        |-- putObject() ------->|                    |
     |                        |   (original + thumb)  |                    |
     |                        |                       |                    |
     |                        |-- POST image -------->|----(async)-------->|
     |                        |   (webhook callback)  |                    |
     |                        |                       |                    |
     |<-- 202 Accepted -------|                       |                    |
     |   status: PENDING      |                       |                    |
     |                        |                       |                    |
     |                        |<----- callback/poll --|<---- result -------|
     |                        |  (APPROVED/REJECTED)  |                    |
     |                        |-- UPDATE status ----->|                    |
```

#### Strategia MVP (pragmatyczna, tania)

**Warstwa 1 — Automatyczna (API NSFW):**

| Opcja | Koszt | Integracja |
|---|---|---|
| **SightEngine** (rekomendacja) | 500 req/mies. free, potem ~$9/1000 | REST API, webhook callback, najlepsze accuracy |
| Moderatecontent.com | 300 req/mies. free | REST API, prostsze |
| Google Cloud Vision SafeSearch | $1.50/1000 req | gRPC/REST, wymaga konta GCP |

**Rekomendacja: SightEngine** — darmowy tier wystarczy na start, prosty REST, kategorie `nudity`, `gore`, `weapon`, `drug`.

**Warstwa 2 — Panel admina (fallback):**

Każde zdjęcie ze statusem `PENDING` lub automatycznie `REJECTED` trafia do kolejki w panelu admina. Admin może zatwierdzić/odrzucić z notatką.

#### Implementacja w Spring Boot

```java
@Service
public class PhotoModerationService {

    // 1. Upload → S3
    public PortfolioPhoto uploadPhoto(Long crewProfileId, MultipartFile file) {
        // Walidacja: typ (JPEG/PNG), rozmiar (max 5MB), wymiary
        // Generuj thumbnail (Thumbnailator library)
        // Upload original + thumb do S3
        // Zapisz rekord ze statusem PENDING
        // Wyślij do kolejki moderacji (async)
        return savedPhoto;
    }

    // 2. Async moderacja
    @Async
    public void moderatePhoto(Long photoId, String s3Key) {
        // Wywołaj SightEngine API
        // Jeśli nudity.raw > 0.5 || gore > 0.5 → REJECTED (auto)
        // Jeśli nudity.raw > 0.2 → PENDING (do ręcznej weryfikacji)
        // W przeciwnym razie → APPROVED
    }
}
```

**Konfiguracja S3 dla MVP:**

- **Rozwój lokalny:** MinIO (Docker) — 100% kompatybilne z S3 API
- **Produkcja:** AWS S3 lub Cloudflare R2 (tańsze, zero egress fees)
- Spring Boot: `spring-cloud-aws-starter-s3` lub bezpośrednio `software.amazon.awssdk:s3`

---

### 0.3 Architektura płatności

#### Rekomendowany provider: Przelewy24 (P24) lub tpay

| Kryterium | Przelewy24 | tpay | Stripe |
|---|---|---|---|
| Popularne w PL | ✅ Dominujący | ✅ Bardzo popularny | ⚠️ Mniej znany wśród MŚP |
| BLIK | ✅ | ✅ | ✅ (od niedawna) |
| Karty | ✅ | ✅ | ✅ |
| Webhook-y | ✅ | ✅ | ✅ |
| Prowizja | ~1.2-1.9% | ~1.2-1.5% | 1.5% + 0.25€ |
| Integracja | REST API | REST API | SDK + REST |

**Rekomendacja: Przelewy24** — standard rynku PL, klienci znają i ufają.

#### Flow płatności (pakiet ogłoszenia)

```
Ekipa                    Frontend           Spring Boot API          Przelewy24
  |                         |                     |                      |
  |-- Wybierz pakiet ------>|                     |                      |
  |                         |-- POST /payments -->|                      |
  |                         |   {packageId: 3}    |                      |
  |                         |                     |-- Zarejestruj tx --->|
  |                         |                     |<-- sessionId --------|
  |                         |<-- redirectUrl ------|                      |
  |<-- Redirect do P24 -----|                     |                      |
  |                         |                     |                      |
  |-- Płatność w P24 ------>|                     |                      |
  |                         |                     |<-- Webhook (status) -|
  |                         |                     |                      |
  |                         |                     |-- Aktywuj subskrypcję|
  |                         |                     |-- Ustaw expires_at   |
  |<-- Redirect na portal --|<-- 200 OK ----------|                      |
```

#### Kluczowe zasady implementacji

1. **Idempotentność webhooków** — P24 może wysłać webhook wielokrotnie. Sprawdzaj `provider_tx_id` przed przetworzeniem.
2. **Weryfikacja podpisu** — każdy webhook musi być zweryfikowany CRC32/SHA384 (P24 docs).
3. **Scheduled job wygaszania:**

```java
@Scheduled(cron = "0 0 * * * *")  // co godzinę
public void deactivateExpiredSubscriptions() {
    subscriptionRepo.deactivateExpired(Instant.now());
    // UPDATE crew_subscriptions SET is_active = false
    // WHERE expires_at < NOW() AND is_active = true
    // + UPDATE crew_profiles SET is_visible = false WHERE ...
}
```

4. **Tabela `payments` jako audit log** — nigdy nie usuwaj rekordów, status `REFUNDED` dla zwrotów.

---

## Faza 1 — Definicja i Zakres MVP

### 1.1 User Stories (wg roli)

#### Gość (niezalogowany)

| ID | User Story | Priorytet |
|---|---|---|
| G-01 | Jako gość mogę przeglądać listę ekip remontowych z filtrami (miasto, kategoria usług) | MUST |
| G-02 | Jako gość mogę zobaczyć profil ekipy (opis, portfolio, oceny, dane kontaktowe) | MUST |
| G-03 | Jako gość mogę zarejestrować się jako Klient lub Ekipa | MUST |
| G-04 | Jako gość widzę średnią ocenę ekipy i liczbę opinii na liście | MUST |

#### Klient (zalogowany, rola CLIENT)

| ID | User Story | Priorytet |
|---|---|---|
| K-01 | Jako klient mogę wystawić ocenę (1-5) i opinię tekstową po zakończeniu współpracy | MUST |
| K-02 | Jako klient mogę edytować/usunąć swoją opinię | SHOULD |
| K-03 | Jako klient widzę dane kontaktowe ekipy (telefon, email) na jej profilu | MUST |

#### Ekipa Remontowa (zalogowany, rola CREW)

| ID | User Story | Priorytet |
|---|---|---|
| E-01 | Jako ekipa mogę stworzyć i edytować profil (nazwa, opis, miasto, kategorie usług, dane kontaktowe) | MUST |
| E-02 | Jako ekipa mogę dodać/usunąć zdjęcia do portfolio (max 20 na MVP) | MUST |
| E-03 | Jako ekipa mogę wykupić pakiet ogłoszenia (7/14/30/365 dni) — mój profil staje się widoczny | MUST |
| E-04 | Jako ekipa mogę dokupić Boost (wyższe pozycjonowanie w wynikach wyszukiwania) | SHOULD |
| E-05 | Jako ekipa widzę status moderacji każdego zdjęcia (Oczekuje/Zatwierdzone/Odrzucone) | MUST |
| E-06 | Jako ekipa widzę datę wygaśnięcia mojego pakietu i mogę go przedłużyć | MUST |

#### Administrator (rola ADMIN)

| ID | User Story | Priorytet |
|---|---|---|
| A-01 | Jako admin mam panel z kolejką zdjęć do moderacji (PENDING + auto-REJECTED) | MUST |
| A-02 | Jako admin mogę zatwierdzić/odrzucić zdjęcie z opcjonalną notatką | MUST |
| A-03 | Jako admin mogę zablokować/odblokować profil ekipy | MUST |
| A-04 | Jako admin widzę listę płatności i ich statusy | SHOULD |
| A-05 | Jako admin widzę dashboard: liczba ekip, klientów, aktywnych pakietów, przychód | SHOULD |

---

### 1.2 Krytyczne endpointy REST API

#### Autentykacja i rejestracja

```
POST   /api/auth/register          – rejestracja (email, hasło, rola)
POST   /api/auth/login             – login → JWT (access + refresh token)
POST   /api/auth/refresh           – odświeżenie tokenu
GET    /api/auth/verify?token=xxx  – weryfikacja email
POST   /api/auth/forgot-password   – reset hasła (wysyłka linku)
POST   /api/auth/reset-password    – ustawienie nowego hasła
```

#### Profile ekip (publiczne + zarządzanie)

```
GET    /api/crews                  – lista ekip (filtry: city, voivodeship, categoryId, sort, page)
GET    /api/crews/{slug}           – profil publiczny (z portfolio APPROVED + avg rating)
POST   /api/crews                  – [CREW] utwórz profil
PUT    /api/crews/{id}             – [CREW] edytuj profil (owner only)
GET    /api/crews/{id}/stats       – [CREW] moje statystyki (wyświetlenia, kliknięcia)
```

#### Portfolio zdjęć

```
POST   /api/crews/{id}/photos      – [CREW] upload zdjęcia (multipart, max 5MB)
DELETE /api/crews/{id}/photos/{photoId} – [CREW] usuń zdjęcie
GET    /api/crews/{id}/photos      – lista zdjęć (publiczne: tylko APPROVED; owner: wszystkie ze statusem)
```

#### Opinie

```
GET    /api/crews/{id}/reviews         – lista opinii (paginacja)
POST   /api/crews/{id}/reviews         – [CLIENT] dodaj opinię
PUT    /api/crews/{id}/reviews/{revId} – [CLIENT] edytuj swoją opinię
DELETE /api/crews/{id}/reviews/{revId} – [CLIENT] usuń swoją opinię
```

#### Płatności

```
POST   /api/payments/listing       – [CREW] inicjuj płatność za pakiet ogłoszenia
POST   /api/payments/boost         – [CREW] inicjuj płatność za Boost
POST   /api/payments/webhook/p24   – [PUBLIC, IP whitelist] webhook od Przelewy24
GET    /api/payments/my             – [CREW] historia moich płatności
```

#### Moderacja (admin)

```
GET    /api/admin/moderation/photos          – kolejka zdjęć (status=PENDING|REJECTED, paginacja)
PUT    /api/admin/moderation/photos/{id}     – zatwierdź/odrzuć
GET    /api/admin/crews                      – lista ekip z opcją blokowania
PUT    /api/admin/crews/{id}/block           – zablokuj/odblokuj
GET    /api/admin/dashboard                  – statystyki
GET    /api/admin/payments                   – lista płatności
```

#### Kategorie usług

```
GET    /api/categories             – lista wszystkich kategorii (publiczny)
```

---

## Faza 1.5 — Plan Deweloperski (Kamienie Milowe)

**Założenia:** Solo founder, full-time. Sprint = 2 tygodnie. Łączny czas MVP: **~12 tygodni (3 miesiące).**

### Sprint 1 (Tydzień 1–2): Fundament i autoryzacja

**Cel:** Działający szkielet projektu z pełnym flow rejestracji i logowania.

**Backend:**
- Inicjalizacja Spring Boot (Maven/Gradle, PostgreSQL, Flyway migrations)
- Spring Security + JWT (access token 15min, refresh token 7d)
- Encje: `users`
- Endpointy: `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/verify`
- Wysyłka maili weryfikacyjnych (Spring Mail + mailtrap.io na dev)
- Walidacja (Bean Validation), global exception handler, standardowy format błędów API

**Frontend:**
- Inicjalizacja React + TypeScript + React Router + Axios
- Strony: Rejestracja, Logowanie, Weryfikacja emaila
- Auth context (przechowywanie JWT, auto-refresh, protected routes)
- Podstawowy layout (Navbar z przyciskami Login/Register, Footer)

**Deliverable:** Użytkownik może się zarejestrować, dostać email weryfikacyjny, zalogować i zobaczyć chroniony dashboard.

---

### Sprint 2 (Tydzień 3–4): Profile ekip + kategorie usług

**Cel:** Ekipa może stworzyć profil. Gość może przeglądać listę ekip.

**Backend:**
- Encje: `crew_profiles`, `service_categories`, `crew_services`
- Seed data: 15-20 kategorii usług (Hydraulika, Elektryka, Malowanie, Glazura, Ogólnobudowlane...)
- CRUD profilu ekipy + walidacja (unikalny slug, generowany z nazwy)
- Endpoint listy ekip z filtrami: `GET /api/crews?city=&categoryId=&page=&size=&sort=rating`
- Paginacja (Spring Data `Pageable`), sortowanie (rating, data, nazwa)

**Frontend:**
- Formularz tworzenia/edycji profilu ekipy
- Strona listy ekip: karty z nazwą, miastem, kategoriami, średnią oceną
- Filtry: dropdown miasto, multi-select kategorie
- Strona profilu publicznego ekipy (bez zdjęć i opinii — będą w Sprint 3 i 4)

**Deliverable:** Działający katalog ekip z wyszukiwaniem i filtrami.

---

### Sprint 3 (Tydzień 5–6): Portfolio zdjęć + moderacja

**Cel:** Ekipa uploaduje zdjęcia. System moderuje automatycznie. Admin ma panel fallback.

**Backend:**
- Integracja S3 (MinIO lokalne / S3 produkcja) — `AmazonS3Client` bean z profilem Spring
- Upload endpoint (multipart, walidacja MIME, resize thumbnails z Thumbnailator)
- Integracja SightEngine API (async, `@Async` + `CompletableFuture`)
- Logika auto-moderacji (thresholds: `nudity > 0.5 → REJECTED`, `> 0.2 → PENDING`, else `APPROVED`)
- Panel admina: endpointy moderacji

**Frontend:**
- Komponent uploadu zdjęć (drag & drop, podgląd, progress bar)
- Galeria portfolio na profilu ekipy (lightbox)
- Status moderacji zdjęć w dashboardzie ekipy (badge: Oczekuje / Zatwierdzone / Odrzucone)
- Panel admina: widok kolejki moderacji, przyciski Zatwierdź/Odrzuć + pole notatki

**Deliverable:** Pełny flow: upload → auto-moderacja → fallback ręczny → zdjęcie na profilu.

---

### Sprint 4 (Tydzień 7–8): Opinie + wyniki wyszukiwania (ranking)

**Cel:** Klienci oceniają ekipy. Opinie wpływają na ranking. Boost gotowy logicznie.

**Backend:**
- CRUD opinii (walidacja: 1 opinia/klient/ekipa, min 10 znaków komentarz)
- Kalkulacja średniej oceny (zmaterializowane pole `avg_rating` + `review_count` w `crew_profiles`, aktualizowane triggerem lub w serwisie)
- Algorytm sortowania listy ekip: `ORDER BY (has_active_boost DESC, avg_rating DESC, review_count DESC)`
- Endpoint `/api/crews` uwzględnia boost (aktywny boost = wyższa pozycja)

**Frontend:**
- Komponent opinii na profilu ekipy (lista + formularz dodawania)
- Gwiazdki (rating widget)
- Zaktualizowana lista ekip: wyświetlanie średniej oceny, badge "Promowane" dla boostowanych

**Deliverable:** System opinii działa end-to-end. Ranking ekip uwzględnia oceny i boost.

---

### Sprint 5 (Tydzień 9–10): Płatności + aktywacja pakietów

**Cel:** Ekipa może zapłacić za pakiet i boost. Profil aktywuje się automatycznie.

**Backend:**
- Integracja Przelewy24 API (rejestracja transakcji, redirect, webhook)
- Endpoint `/api/payments/listing` i `/api/payments/boost`
- Webhook handler z weryfikacją podpisu + idempotentność
- Logika aktywacji: po `COMPLETED` → utwórz `crew_subscriptions` / `crew_boosts`, ustaw `is_visible = true`
- Scheduled job: wygaszanie expired subscriptions + boosts (co godzinę)
- Sandbox P24 do testów (środowisko testowe P24)

**Frontend:**
- Strona "Wybierz pakiet" (karty z cenami: 7 dni / 14 dni / 30 dni / rok)
- Strona "Kup Boost" (analogicznie)
- Flow: wybór → redirect do P24 → powrót na stronę sukcesu/błędu
- Dashboard ekipy: info o aktywnym pakiecie, data wygaśnięcia, przycisk "Przedłuż"

**Deliverable:** Monetyzacja działa end-to-end. Ekipa płaci → profil widoczny → wygasa → profil znika.

---

### Sprint 6 (Tydzień 11–12): Polish, testy, Admin dashboard, SEO

**Cel:** Produkcja-ready. Polerowanie UX. Testy. Przygotowanie do launch.

**Backend:**
- Admin dashboard endpoint (statystyki: users count, active subscriptions, revenue)
- Testy integracyjne (Testcontainers + PostgreSQL) dla krytycznych flow: rejestracja, płatność, moderacja
- Rate limiting (Bucket4j lub Spring Cloud Gateway) na endpointach publicznych
- CORS config produkcyjna
- Logowanie (SLF4J + structured logging)
- Actuator health checks

**Frontend:**
- Admin dashboard (wykres przychodów, metryki)
- Responsywność (mobile-first — ekipy remontowe używają telefonów)
- SEO: meta tagi, Open Graph, sitemap.xml, robots.txt
- Error boundaries, loading states, empty states
- Strona regulaminu i polityki prywatności (RODO!)
- Landing page (hero, jak to działa, CTA rejestracji)

**Deliverable:** Aplikacja gotowa do wdrożenia. Kluczowe flow przetestowane. Landing page gotowy.

---

## Faza 2 — Launch & Przygotowanie na Skalowanie

### 2.1 CI/CD Pipeline

#### Architektura pipeline'u

```
GitHub repo (monorepo)
├── /backend     (Spring Boot)
├── /frontend    (React + TS)
└── /infra       (Docker, nginx, scripts)

GitHub Actions Workflow:

┌─────────────┐     ┌─────────────┐     ┌──────────────┐     ┌───────────┐
│   Push to   │────>│    Build    │────>│    Test      │────>│  Deploy   │
│   main      │     │  & Lint     │     │  & Scan     │     │           │
└─────────────┘     └─────────────┘     └──────────────┘     └───────────┘

Backend:                          Frontend:
- mvn clean verify                - npm ci && npm run lint
- Testcontainers (integration)    - npm run build
- Docker build + push             - Docker build (nginx) + push
- SonarQube (opcjonalnie)         - Lighthouse CI (opcjonalnie)
```

#### docker-compose.prod.yml (docelowy stos)

```yaml
services:
  api:
    image: ghcr.io/twoj-user/app-backend:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://db:5432/appdb
      - S3_ENDPOINT=https://s3.amazonaws.com
      - P24_MERCHANT_ID=${P24_MERCHANT_ID}
    ports:
      - "8080:8080"
    depends_on:
      - db
  frontend:
    image: ghcr.io/twoj-user/app-frontend:latest
    ports:
      - "3000:80"
  db:
    image: postgres:16
    volumes:
      - pgdata:/var/lib/postgresql/data
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./certs:/etc/letsencrypt
```

#### Rekomendacja hostingowa

| Opcja | Koszt/mies. (start) | Zalety | Wady |
|---|---|---|---|
| **VPS (Hetzner/OVH)** | ~€10-20 | Pełna kontrola, niski koszt, serwery w DE/PL | Zarządzasz sam |
| AWS (EC2 + RDS) | ~$50-80 | Skalowalność, S3 "w pakiecie" | Drożej, złożoność |
| Railway/Render (PaaS) | ~$20-40 | Zero DevOps, auto-deploy | Mniej kontroli |

**Rekomendacja na start: Hetzner VPS (CX31/CX41)** — 8GB RAM, 4 vCPU, ~€15/mies. Wystarczy na tysiące użytkowników. Docker Compose na jednym serwerze. Gdy ruch przekroczy możliwości — migracja na AWS/GCP.

---

### 2.2 Przygotowanie architektoniczne pod wewnętrzny czat

Tabele `conversations` i `messages` już istnieją w schemacie (Faza 0). Poniżej — decyzje architektoniczne, które podejmij **teraz**, żeby za 6 miesięcy nie robić rewolucji.

#### Co zrobić TERAZ (Faza MVP)

1. **Tabele w DB** — już gotowe (patrz schemat wyżej).

2. **Abstrakcja w kodzie** — stwórz pakiet `messaging` z pustymi interfejsami:

```java
// src/main/java/com/app/messaging/ConversationService.java
public interface ConversationService {
    // Na razie puste — implementacja w Fazie 2+
}
```

3. **Dependency na WebSocket** — NIE dodawaj teraz. Ale upewnij się, że architektura (nginx, Docker) nie blokuje upgrade'u HTTP → WebSocket:

```nginx
# nginx.conf — dodaj od razu, nawet jeśli nie używasz
location /ws/ {
    proxy_pass http://api:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

4. **JWT zawiera user ID** — już masz. Czat będzie używał tego samego tokenu do autoryzacji WebSocket handshake.

#### Co zrobić W PRZYSZŁOŚCI (Faza 2+)

1. Dodaj `spring-boot-starter-websocket` + STOMP.
2. `ConversationController` + `MessageController` (REST do historii + WS do real-time).
3. Prosta implementacja: STOMP over WebSocket, in-memory broker. Przy skali → Redis pub/sub lub RabbitMQ.
4. Powiadomienia push (FCM/APNs) jeśli będzie apka mobilna.

#### Dlaczego NIE robić czatu w MVP

- Czat to ~3-4 tygodnie dodatkowej pracy (WebSocket, real-time, powiadomienia, UI).
- MVP waliduje hipotezę biznesową: "czy ekipy zapłacą za widoczność?". Do tego wystarczy telefon/email.
- Czat to feature fazy growth, nie fazy walidacji.

---

### 2.3 Checklist przed launch

| Kategoria | Zadanie | Status |
|---|---|---|
| **Prawne** | Regulamin serwisu | ☐ |
| **Prawne** | Polityka prywatności (RODO) | ☐ |
| **Prawne** | Cookie banner + consent management | ☐ |
| **Prawne** | Rejestracja firmy / działalność gospodarcza | ☐ |
| **Bezpieczeństwo** | HTTPS (Let's Encrypt) | ☐ |
| **Bezpieczeństwo** | Rate limiting na auth endpoints | ☐ |
| **Bezpieczeństwo** | OWASP Top 10 audit (SQLi, XSS, CSRF) | ☐ |
| **Bezpieczeństwo** | Backup PostgreSQL (pg_dump cron → S3) | ☐ |
| **Monitoring** | Uptime monitoring (UptimeRobot / Kuma) | ☐ |
| **Monitoring** | Error tracking (Sentry — free tier) | ☐ |
| **Monitoring** | Logi aplikacji (rotacja, retencja) | ☐ |
| **SEO** | Sitemap.xml + robots.txt | ☐ |
| **SEO** | Meta tagi + Open Graph (dla udostępniania profili) | ☐ |
| **SEO** | Google Search Console + Analytics | ☐ |
| **Biznes** | Konto Przelewy24 produkcyjne | ☐ |
| **Biznes** | 10-20 ekip testowych (seed data lub beta-testerzy) | ☐ |
| **Biznes** | Landing page z CTA | ☐ |
| **Wydajność** | CDN dla statycznych assetów (Cloudflare — free) | ☐ |
| **Wydajność** | Gzip/Brotli compression (nginx) | ☐ |
| **Wydajność** | Cache: HTTP cache headers na zdjęcia portfolio | ☐ |

---

### 2.4 Post-Launch Roadmap (Faza 3+)

Kierunki rozwoju po walidacji MVP, posortowane wg wpływu na biznes:

1. **Wewnętrzny czat** (WebSocket + STOMP) — retention, engagement, prowizja od zleceń
2. **System zleceń** — klient publikuje "zapytanie ofertowe", ekipy odpowiadają ceną → marketplace effect
3. **Weryfikacja ekip** (NIP/REGON check via GUS API, certyfikaty) — trust & safety
4. **Aplikacja mobilna** (React Native / Flutter) — ekipy pracują w terenie
5. **Powiadomienia** (email + push) — nowa opinia, wygasający pakiet, nowe zlecenie
6. **Geolokalizacja** (PostGIS) — wyszukiwanie "ekipy w promieniu 30km"
7. **Program poleceń** — ekipa poleca ekipę → rabat na pakiet
8. **A/B testing cen pakietów** — optymalizacja revenue
