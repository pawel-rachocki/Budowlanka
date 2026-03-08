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

Pełny schemat SQL: zobacz @docs/database-schema.sql

## Kluczowe integracje

- Przelewy24: REST API, webhook, CRC32/SHA384 weryfikacja
- SightEngine: REST API, async via @Async, thresholds nudity/gore/weapon
- S3: MinIO (dev), Cloudflare R2 (prod)
- Mail: Spring Mail + Mailtrap (dev) / SMTP prod

## Kontrakty API

Pełna lista endpointów: zobacz @docs/api-contracts.md

### Podsumowanie endpointów

Auth: POST /api/auth/register, /login, /refresh, /verify, /forgot-password, /reset-password
Crews: GET /api/crews (lista+filtry), GET /api/crews/{slug}, POST/PUT /api/crews
Photos: POST/DELETE /api/crews/{id}/photos, GET /api/crews/{id}/photos
Reviews: GET/POST/PUT/DELETE /api/crews/{id}/reviews
Payments: POST /api/payments/listing, /boost, /webhook/p24, GET /api/payments/my
Admin: GET/PUT /api/admin/moderation/photos, /crews, /dashboard, /payments
Categories: GET /api/categories
