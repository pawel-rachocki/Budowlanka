# Portal Ekipy Remontowe

Marketplace łączący ekipy remontowe/budowlane z klientami w Polsce.
Monorepo: backend (Java/Spring Boot) + frontend (React/TypeScript).

## Architektura

- Backend: Java 21, Spring Boot 4.x, Spring Security + JWT (Nimbus JOSE), PostgreSQL 17
- Frontend: React z hooks, TypeScript strict, React Router, Axios, TanStack Query
- Storage: S3-compatible (MinIO dev / Cloudflare R2 prod)
- Płatności: Przelewy24
- Moderacja zdjęć: SightEngine API (async)
- Migracje DB: Flyway
- Formatowanie kodu: Spotless (Google Java Format) — `mvn spotless:apply`

## Kluczowe komendy

- Backend build: `cd backend && mvn clean verify`
- Backend run: `cd backend && mvn spring-boot:run`
- Backend testy: `cd backend && mvn test`
- Backend format: `cd backend && mvn spotless:apply`
- Frontend install: `cd frontend && npm ci`
- Frontend dev: `cd frontend && npm run dev`
- Frontend build: `cd frontend && npm run build`
- Frontend lint: `cd frontend && npm run lint`
- Frontend typecheck: `cd frontend && npx tsc --noEmit`
- Baza lokalna: `cd infra && docker compose up -d`

## Struktura kodu

- `backend/src/main/java/com/budowlanka/` — główny pakiet Java
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
- Testy: każdy serwis ma testy jednostkowe, krytyczne flow mają testy integracyjne

## Reguły bezpieczeństwa

- NIGDY nie commituj credentials, kluczy API, haseł
- NIGDY git push --force na main
- NIGDY rm -rf bez potwierdzenia
- Hasła: BCrypt. JWT (Nimbus JOSE): access 15min, refresh 7d
- Walidacja: Bean Validation na każdym DTO

## Specyfikacja i docs

- Pełna spec MVP: @SPEC.md
- Schemat bazy: @docs/database-schema.sql
- Kontrakty API: @docs/api-contracts.md
- Decyzje arch: @docs/architecture.md
