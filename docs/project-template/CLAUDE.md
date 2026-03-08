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
