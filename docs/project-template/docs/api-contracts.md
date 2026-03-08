# Kontrakty REST API

Dokument aktualizowany przy każdym nowym endpoincie.

## Auth

### POST /api/auth/register
- Request: `{ email: string, password: string, role: "CLIENT" | "CREW" }`
- Response 201: `{ id: number, email: string, role: string }`
- Errors: 409 (email exists), 400 (validation)

### POST /api/auth/login
- Request: `{ email: string, password: string }`
- Response 200: `{ accessToken: string, refreshToken: string, user: { id, email, role } }`
- Errors: 401 (bad credentials), 403 (email not verified)

### POST /api/auth/refresh
- Request: `{ refreshToken: string }`
- Response 200: `{ accessToken: string, refreshToken: string }`
- Errors: 401 (invalid/expired token)

### GET /api/auth/verify?token=xxx
- Response 200: `{ message: "Email verified" }`
- Errors: 400 (invalid/expired token)

---

## Crews

TODO: dodaj po Sprint 2

## Photos

TODO: dodaj po Sprint 3

## Reviews

TODO: dodaj po Sprint 4

## Payments

TODO: dodaj po Sprint 5

## Admin

TODO: dodaj po Sprint 3 (moderacja) i Sprint 6 (dashboard)
