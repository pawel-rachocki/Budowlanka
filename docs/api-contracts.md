# Kontrakty API

Base URL: `http://localhost:8080/api`

Format błędów:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-03-08T10:00:00Z",
  "errors": ["email: must not be blank"]
}
```

---

## Auth — `/api/auth`

### POST /api/auth/register
Request:
```json
{ "email": "jan@example.com", "password": "haslo123", "role": "CLIENT" }
```
Response `201`:
```json
{ "message": "Rejestracja udana. Sprawdź email, aby aktywować konto." }
```

### GET /api/auth/verify?token={token}
Response `200`: `{ "message": "Email zweryfikowany. Możesz się zalogować." }`

### POST /api/auth/login
Request: `{ "email": "...", "password": "..." }`
Response `200`: `{ "accessToken": "...", "tokenType": "Bearer" }`
Set-Cookie: `refresh_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/api/auth/refresh`

### POST /api/auth/refresh
No body — refreshToken is read from the `refresh_token` httpOnly cookie (sent automatically).
Response `200`: `{ "accessToken": "..." }`

### POST /api/auth/logout
Header: `Authorization: Bearer {accessToken}`
Response `204`
Set-Cookie: `refresh_token=; HttpOnly; Secure; SameSite=Strict; Path=/api/auth/refresh; Max-Age=0`

---

<!-- Kolejne endpointy dodawaj tutaj w miarę implementacji -->
