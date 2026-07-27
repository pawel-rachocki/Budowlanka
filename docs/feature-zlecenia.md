# Moduł zleceń — odwrócony marketplace

Status: **koncepcja zatwierdzona 2026-07-18** — decyzje podjęte, implementacja
po zakończeniu Sprint 6 (launch prep). Podział: Sprint 7 (zlecenia + oferty +
alerty) i Sprint 8 (czat). Ticket: REM-182.

## Motywacja

Obecny model: klient szuka ekipy na liście i sam się kontaktuje. Rozmowy z
potencjalnymi klientami pokazały, że wygodniejszy jest model odwrotny: klient
opisuje zlecenie (formularz), a to ekipy odzywają się do niego z ofertami.
Mniejsze zaangażowanie po stronie klienta, więcej leadów po stronie ekip
(dodatkowa wartość płatnego pakietu).

Tak działa rynek w PL: Fixly (OLX), Oferteo — klient wystawia zapytanie,
wykonawcy dostają powiadomienia o pasujących leadach i wysyłają oferty.

## Podjęte decyzje (2026-07-18)

| # | Decyzja | Wybór |
|---|---|---|
| 1 | Monetyzacja | Składanie ofert wymaga **aktywnej subskrypcji** (pakiet listing ze Sprint 5). Zero nowych mechanizmów płatności; pakiet = widoczność + dostęp do leadów. |
| 2 | Widoczność listy zleceń | **Tylko zalogowani CREW.** Bez SEO na zleceniach (świadomy trade-off — ochrona leadów przed scrapingiem). Klient widzi wyłącznie własne zlecenia i oferty do nich. |
| 3 | CREW bez subskrypcji | **Widzi listę i szczegóły**, ale „Wyślij ofertę" pokazuje CTA „Wykup pakiet" — lista działa jako upsell (ekipa widzi konkretne uciekające leady). |
| 4 | Komunikacja | **Mini-czat, wątek per oferta** — oferta otwiera rozmowę powiązaną ze zleceniem (jak Fixly/Oferteo). Czat wchodzi w Sprint 8; w Sprint 7 klient dostaje treść oferty mailem od platformy. |
| 5 | Moderacja | Zdjęcia: istniejący pipeline SightEngine (jak portfolio). Tekst: publikacja od razu + przycisk „Zgłoś zlecenie" + kolejka zgłoszeń w panelu admina. Bez pre-moderacji tekstu. |
| 6 | Podział pracy | **Dwa sprinty.** Sprint 7 kończy się działającym flow end-to-end (oferta trafia do klienta mailem), Sprint 8 dokłada czat. |

Defaulty (do weryfikacji w praniu):
- Anty-spam: max **5 otwartych zleceń** per klient; rate limit (Bucket4j):
  tworzenie zlecenia 3/h/IP, wysłanie oferty 10/h/IP.
- Auto-wygasanie: zlecenie `OPEN` → `EXPIRED` po **30 dniach** (scheduled job,
  wzorzec ze Sprint 5); klient może przedłużyć/zamknąć wcześniej.
- Szablony ofert: do **3 nazwanych** szablonów per ekipa.
- Alerty mailowe dla ekip: **instant** per pasujące zlecenie, deduplikacja
  (1 mail/ekipę/zlecenie niezależnie od liczby pasujących subskrypcji),
  cap 10 maili/dzień/ekipę, stopka „zmień ustawienia powiadomień".
  Digest dzienny jako opcja — dopiero po MVP modułu.

## User flow

### Klient (CLIENT)
1. Wypełnia formularz zlecenia: tytuł, opis prac, kategoria usługi
   (istniejące `service_categories`), miasto + województwo (opcjonalnie
   dzielnica/okolica w polu tekstowym), opcjonalnie budżet i termin.
2. Opcjonalnie dodaje zdjęcia (reużycie pipeline'u S3 + moderacja SightEngine).
3. Publikuje (tekst widoczny od razu). Zlecenie widzą zalogowane ekipy.
4. Dostaje oferty — mail **od platformy** z treścią oferty i linkiem do
   profilu ekipy (Sprint 7); od Sprint 8 odpowiada w czacie przy ofercie.
5. Może zamknąć zlecenie (znalazłem wykonawcę / nieaktualne).

### Ekipa (CREW)
1. Przegląda listę zleceń z filtrami: kategoria, miasto, województwo, data
   dodania (spójne z filtrami listy ekip). Lista dostępna dla każdej
   zalogowanej ekipy — także bez subskrypcji (upsell).
2. Z aktywną subskrypcją wysyła ofertę: treść + (opcjonalnie) cena
   orientacyjna. Bez subskrypcji: CTA „Wykup pakiet". Ekipa NIE widzi
   maila klienta — kontakt wyłącznie przez platformę.
3. Może zapisać **szablon oferty** (do 3 nazwanych) — w profilu lub
   „zapisz jako szablon" przy składaniu oferty.
4. Ustawia **subskrypcje alertów**: dowolna kombinacja filtrów (kategoria
   i/lub miasto i/lub województwo), wiele subskrypcji na ekipę. Nowe
   pasujące zlecenie → mail (instant, patrz defaulty).

## Model danych (szkic — finalne kolumny przy migracjach)

```sql
CREATE TABLE job_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_user_id  UUID NOT NULL REFERENCES users(id),
    category_id     UUID NOT NULL REFERENCES service_categories(id),
    title           VARCHAR(150) NOT NULL,
    description     TEXT NOT NULL,
    city            VARCHAR(100) NOT NULL,
    voivodeship     VARCHAR(50)  NOT NULL,
    area_details    VARCHAR(150),          -- np. "Bemowo", opcjonalne
    budget_pln      NUMERIC(10,2),         -- opcjonalny
    preferred_date  DATE,                  -- opcjonalny termin
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'CLOSED', 'EXPIRED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- indeksy: (status, created_at), (voivodeship), (city), (category_id)

CREATE TABLE job_request_photos (
    -- analogiczne do portfolio_photos: storage_key, thumbnail_key,
    -- moderation_status (reużycie SightEngine flow)
);

CREATE TABLE job_offers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_request_id  UUID NOT NULL REFERENCES job_requests(id),
    crew_profile_id UUID NOT NULL REFERENCES crew_profiles(id),
    message         TEXT NOT NULL,
    price_pln       NUMERIC(10,2),         -- orientacyjna, opcjonalna
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (job_request_id, crew_profile_id)   -- 1 oferta na zlecenie
);

CREATE TABLE offer_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_profile_id UUID NOT NULL REFERENCES crew_profiles(id),
    name            VARCHAR(100) NOT NULL,
    body            TEXT NOT NULL
    -- limit 3/ekipę egzekwowany w serwisie
);

CREATE TABLE job_alert_subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_profile_id UUID NOT NULL REFERENCES crew_profiles(id),
    category_id     UUID REFERENCES service_categories(id),  -- NULL = każda
    city            VARCHAR(100),                            -- NULL = każde
    voivodeship     VARCHAR(50),                             -- NULL = każde
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE job_request_reports (        -- "Zgłoś zlecenie"
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_request_id   UUID NOT NULL REFERENCES job_requests(id),
    reporter_user_id UUID NOT NULL REFERENCES users(id),
    reason           TEXT NOT NULL,
    resolved         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (job_request_id, reporter_user_id)
);
```

### Czat (Sprint 8) — zmiana placeholder-tabel

Decyzja: **wątek per oferta**. Placeholder `conversations` ma
`UNIQUE(client_user_id, crew_user_id)` — do zmiany migracją:

```sql
ALTER TABLE conversations ADD COLUMN job_offer_id UUID REFERENCES job_offers(id);
-- DROP dotychczasowego UNIQUE(client_user_id, crew_user_id)
-- UNIQUE(job_offer_id) — jeden wątek na ofertę
-- (przyszły czat ogólny poza zleceniami: job_offer_id NULL + osobna reguła)
```

`messages` zostaje bez zmian. Tabele są puste (API nigdy nie istniało),
więc migracja jest bezpieczna.

## Zakres sprintów

### Sprint 7 — zlecenia + oferty + alerty (2 tyg.)
- Migracje: `job_requests`, `job_request_photos`, `job_offers`,
  `offer_templates`, `job_alert_subscriptions`, `job_request_reports`
- Endpointy: CRUD zleceń (CLIENT), lista zleceń + filtry (CREW),
  zdjęcia zleceń (upload/moderacja — reużycie), CRUD ofert
  (CREW + gate subskrypcji), moje oferty, oferty do mojego zlecenia (CLIENT),
  CRUD szablonów, CRUD subskrypcji alertów, report + admin queue zgłoszeń
- Maile: alert o zleceniu (ekipa, instant + dedup + cap), nowa oferta (klient,
  treść oferty + link do profilu ekipy)
- Scheduled job: auto-EXPIRED po 30 dniach
- Rate limiting: rozszerzenie Bucket4j o nowe endpointy
- Frontend: formularz zlecenia (+ zdjęcia drag&drop — reużycie), lista zleceń
  z filtrami (CREW), szczegóły zlecenia + formularz oferty (+ szablony),
  „Moje zlecenia" z ofertami (CLIENT), ustawienia alertów (CREW),
  admin: kolejka zgłoszeń
- Deliverable: klient wystawia zlecenie → ekipa dostaje alert → wysyła ofertę
  → klient dostaje maila z ofertą i kontaktuje się z ekipą

### Sprint 8 — czat przy ofertach (2 tyg.)
- Migracja `conversations` (job_offer_id, zmiana UNIQUE)
- Endpointy: wątki, wiadomości (paginacja), oznaczanie przeczytanych
- Mail „nowa wiadomość" (z throttlingiem — nie per wiadomość w konwersacji)
- Frontend: widok wątku przy ofercie, lista rozmów, badge nieprzeczytanych
- MVP czatu bez websocketów — polling/refetch (TanStack Query); realtime
  to ewentualna Faza 3

## Zależności / reużycie istniejących modułów

- `service_categories` — te same kategorie do zleceń i filtrów.
- S3 + SightEngine pipeline — zdjęcia zleceń jak portfolio.
- Spring Mail — już jest (weryfikacja emaila); dochodzą nowe typy maili.
- Gate subskrypcji — reużycie logiki `hasActiveSubscription` ze Sprint 5.
- Bucket4j rate limiting (Sprint 6) — rozszerzenie o endpointy zleceń.
- Scheduled jobs (Sprint 5, wygaszanie subskrypcji) — wzorzec dla wygasania
  zleceń.
- Panel admina — kolejka zgłoszeń obok istniejącej kolejki moderacji zdjęć.

## Przewagi konkurencyjne vs Oferteo/Fixly (backlog pomysłów, 2026-07-18)

Pomysły na wyróżnienie się, budowane na znanych bólach użytkowników
konkurencji. Pogrupowane wg horyzontu; kandydaci do Sprint 7 oznaczeni —
włączenie do zakresu wymaga osobnej decyzji (nie są jeszcze w scope).

### Wynikają z podjętych decyzji — do komunikowania w marketingu

1. **Klient nie jest bombardowany telefonami.** Główny ból Oferteo: numer
   klienta trafia do N wykonawców. U nas kontakt wyłącznie przez platformę —
   wybić na landingu („Twój numer nie trafia do nikogo").
2. **Ekipa nie płaci za martwe leady.** Największy ból wykonawców u
   konkurencji: płacisz za kontakt, klient nie odbiera. Flat abonament =
   zero ryzyka per lead — główny sales pitch do ekip.

### Tanie — kandydaci do Sprint 7

3. **Limit ofert na zlecenie (np. 5).** Klient nie tonie w ofertach, każdy
   lead ma realną wartość, „zostały 2 miejsca" = pilność napędzająca
   subskrypcje i szybkie odpowiadanie (Fixly robi to celowo).
   Koszt: kolumna + walidacja.
4. **Weryfikacja NIP przez API GUS / Białą Listę.** Pole `nip` już jest w
   `crew_profiles`, API REGON darmowe. Badge „Zweryfikowana firma" — tani,
   mocny sygnał zaufania.
5. **Statystyki responsywności na profilu ekipy** — „odpowiada zwykle w 2h",
   liczba wysłanych ofert. Dane z `job_offers` za darmo; motywuje ekipy,
   pomaga klientowi wybrać.

### Średnie — Sprint 8+

6. **Opinie zweryfikowane zleceniem** (najmocniejszy kandydat). Klient
   zamyka zlecenie wybierając „wybrałem ekipę X" → po ~2 tyg. mail „oceń
   współpracę" → opinia z badge „po zrealizowanym zleceniu" (powiązanie
   review↔job_offer). Zabija problem fejkowych opinii i domyka flywheel:
   zlecenie → oferta → realizacja → wiarygodna opinia → lepszy ranking →
   więcej zleceń. Strukturalna przewaga: model konkurencji (sprzedaż
   kontaktu, rozmowa ucieka poza platformę) nie widzi, czy robota doszła
   do skutku — nasz widzi.
7. **Strukturalna oferta zamiast wolnego tekstu** — opcjonalne pozycje
   kosztorysu (robocizna/materiały/termin). Klient porównuje oferty obok
   siebie; u konkurencji oferty to chaos.
8. **Kalendarz dostępności ekipy** — „wolne okna", filtr „dostępni w
   sierpniu". Nikt w PL nie robi tego dobrze, a przy remontach termin bywa
   ważniejszy niż cena.

### Większe zakłady — Faza 3, po walidacji rynku

9. **Agregowane widełki cenowe z ofert** — „ile kosztuje remont łazienki
   w Warszawie" z prawdziwych danych. Unikalny content + magnes SEO
   (odzyskuje SEO utracone przez zamkniętą listę zleceń, bez ujawniania
   leadów).
10. **AI-asystent brief'u** — klient odpowiada na kilka pytań, powstaje
    dobrze ustrukturyzowane zlecenie (kategoria, zakres, sugerowane
    widełki). Rozwiązuje problem słabych opisów, których ekipy nie umieją
    wycenić. Naturalne miejsce na Claude API.
11. **Płatności za realizację / escrow** — zaliczka trzymana do odbioru
    prac. Ogromna wartość (zaufanie = problem nr 1 branży), ale duża
    złożoność prawno-finansowa — na razie tylko notatka w wizji.
