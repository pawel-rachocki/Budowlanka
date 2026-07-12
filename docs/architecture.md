# Decyzje Architektoniczne

## Stack

| Komponent | Technologia | Uzasadnienie |
|---|---|---|
| Backend | Spring Boot 4.x, Java 21 | LTS, virtual threads, najnowszy ekosystem |
| JWT | Nimbus JOSE JWT | Wbudowany w Spring Security, kompatybilny z Jackson 3.x (SB4) |
| Baza | PostgreSQL 17 | ACID, jsonb, full-text search na przyszłość |
| Migracje | Flyway | Wersjonowanie schematu, rollback |
| Frontend | React + TypeScript + Vite | SPA, fast HMR, strict typing |
| Style | Tailwind CSS v4 | Utility-first, mobile-first |
| Storage | MinIO (dev) / Cloudflare R2 (prod) | S3-compatible, tanie |
| Płatności | Przelewy24 | Dominujący provider w Polsce |
| Moderacja | SightEngine | NSFW detection, async |

## Kluczowe decyzje

| Decyzja | Uzasadnienie |
|---|---|
| `role` w tabeli `users` (nie osobne tabele) | Dla MVP wystarczy RBAC. Przy skali → osobna tabela roles + M:N |
| `slug` w `crew_profiles` | SEO — profil pod `/ekipy/kowalski-remonty-warszawa` |
| `is_visible` sterowany automatycznie | Profil widoczny = aktywny `crew_subscriptions` z `expires_at > NOW()` |
| Tabele czatu od razu | Istnieją od dnia 1, ale API w Fazie 2. Zero migracji strukturalnych później |
| Refresh token w DB | Możliwość unieważnienia (logout, zmiana hasła). Trade-off: query przy każdym refresh |
| Nimbus JOSE zamiast jjwt | jjwt 0.12.x niezgodny z Jackson 3.x używanym przez Spring Boot 4.x |
| CORS origins z env (`CORS_ALLOWED_ORIGINS`) | Dev: default `localhost:5173`. Prod (`application-prod.properties`): brak defaultu — fail-fast przy braku env, nie da się wypuścić proda z originem dev |
| Natywny structured logging SB4 zamiast logstash-logback-encoder | `logging.structured.format.console=logstash` (prod) daje JSON z MDC bez dodatkowej zależności; encoder ciągnie Jackson 2.x obok Jacksona 3 z SB4. Dev: plain pattern z `requestId` przez `logging.pattern.correlation` |
| Request-id: filtr servlet-level (`RequestIdFilter`), nie w security chain | `@Order(HIGHEST_PRECEDENCE)` obejmuje cały security chain — logi z JwtAuthFilter, rate-limit 429 i 401/403 też mają `requestId`. MDC propagowany do `@Async` przez `MdcTaskDecorator` |

## Dev credentials

| Konto | Email | Hasło | Uwaga |
|---|---|---|---|
| Admin | `admin@budowlanka.local` | `admin123` | **Zmień przed deploy na prod!** Seed: `V012__seed_admin_user.sql` |

## Struktura pakietów backendu

```
com.budowlanka/
├── config/      — SecurityConfig, CorsConfig, AsyncConfig
├── auth/        — AuthController, AuthService, JwtService, encje tokenów
├── crew/        — CrewController, CrewService, CrewProfile entity
├── photo/       — PhotoController, PhotoService, S3Service, ModerationService
├── review/      — ReviewController, ReviewService
├── payment/     — PaymentController, WebhookController, Przelewy24Client
├── admin/       — AdminController, moderacja, statystyki
└── common/      — GlobalExceptionHandler, ApiError, BaseEntity
```
