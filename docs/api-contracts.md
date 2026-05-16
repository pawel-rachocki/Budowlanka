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
Response `409` (email już zajęty):
```json
{ "status": 409, "message": "Email jest już zajęty.", "timestamp": "2026-03-08T10:00:00Z" }
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

## Crew — `/api/crew`

### POST /api/crew/profiles
Auth: `Bearer {accessToken}` (rola CREW)
Request:
```json
{
  "companyName": "Kowalski Remonty",
  "description": "Profesjonalne remonty",
  "phone": "600100200",
  "contactEmail": "kontakt@kowalski.pl",
  "city": "Warszawa",
  "voivodeship": "MAZOWIECKIE",
  "serviceRadiusKm": 50,
  "nip": "1234567890",
  "categoryIds": ["uuid1", "uuid2"]
}
```
Response `201`: `CrewProfileResponse` (pełny profil)
Response `400`: błąd walidacji (np. puste `companyName`, nieprawidłowy NIP)
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `409`: profil już istnieje dla tego użytkownika

### GET /api/crew/profiles/me
Auth: `Bearer {accessToken}` (rola CREW)
Response `200`: `CrewProfileResponse`
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `404`: profil nie istnieje

### PUT /api/crew/profiles/me
Auth: `Bearer {accessToken}` (rola CREW)
Request: pola opcjonalne (patch semantics) — te same co POST, wszystkie nullable
Response `200`: `CrewProfileResponse`
Response `400`: błąd walidacji
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `404`: profil nie istnieje

### GET /api/crew/profiles/{slug}
Auth: opcjonalny (`Bearer {accessToken}`)
Response `200`: `CrewProfileResponse`
- Zalogowany użytkownik: pełne dane — `phone` i `contactEmail` są wypełnione
- Anonim (brak tokenu): `phone` i `contactEmail` są `null`
- Ukryty profil (`is_visible=false`): `404` dla każdego poza właścicielem
Response `404`: profil nie istnieje lub jest ukryty dla tego wywołującego

### GET /api/crew/profiles?city=&voivodeship=&categoryId=&page=0&size=20
Auth: brak (publiczny)
Constraints: max `size=100`; zwraca tylko profile z `is_visible=true`
Response `200`:
```json
{
  "content": [ { "id": "...", "companyName": "...", "slug": "...", "city": "...", "voivodeship": "...", "avgRating": 4.5, "reviewCount": 12, "categories": [] } ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

---

## Photos — `/api/crew/photos`

### POST /api/crew/photos
Auth: `Bearer {accessToken}` (rola CREW)
Content-Type: `multipart/form-data`
Parts: `file` (obraz JPEG/PNG/WebP, max 5 MB), `caption` (opcjonalny, string)
Response `202`: `PhotoResponse` — zdjęcie przyjęte, moderacja w toku (`moderationStatus: PENDING`)
Response `400`: nieprawidłowy typ pliku lub rozmiar
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `413`: plik przekracza 5 MB
Response `422`: osiągnięto limit 20 zdjęć dla profilu

### GET /api/crew/photos/me
Auth: `Bearer {accessToken}` (rola CREW)
Response `200`: `List<PhotoResponse>` — wszystkie zdjęcia (każdy status moderacji), posortowane od najnowszych
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `404`: profil ekipy nie istnieje

### DELETE /api/crew/photos/{id}
Auth: `Bearer {accessToken}` (rola CREW)
Response `204`: zdjęcie usunięte (plik z S3 usunięty po commicie transakcji)
Response `401`: brak lub nieprawidłowy token
Response `403`: zdjęcie nie należy do zalogowanej ekipy
Response `404`: zdjęcie nie istnieje

### GET /api/crew/profiles/{slug}/photos
Auth: brak (publiczny)
Response `200`: `List<PhotoResponse>` — tylko zdjęcia ze statusem `APPROVED`, pola `moderationStatus` i `moderationNote` są `null`
Response `404`: profil ekipy nie istnieje

`PhotoResponse`:
```json
{
  "id": "uuid",
  "url": "https://cdn.example.com/...",
  "thumbnailUrl": "https://cdn.example.com/...",
  "caption": "Remont kuchni",
  "moderationStatus": "PENDING",
  "moderationNote": null,
  "uploadedAt": "2026-05-01T12:00:00Z"
}
```

---

## Categories — `/api/categories`

### GET /api/categories
Auth: brak (publiczny)
Response `200`:
```json
[
  { "id": "uuid", "name": "Malowanie", "slug": "malowanie" },
  { "id": "uuid", "name": "Tynkowanie", "slug": "tynkowanie" }
]
```

---

## Admin — `/api/admin`

Auth: `Bearer {accessToken}` (rola ADMIN) — wszystkie endpointy

### GET /api/admin/moderation/photos?status=PENDING&page=0&size=20
Response `200`: `PagedResponse<PhotoModerationItemResponse>`
```json
{
  "content": [
    {
      "id": "uuid",
      "originalUrl": "https://cdn.example.com/...",
      "thumbnailUrl": "https://cdn.example.com/...",
      "caption": "Remont kuchni",
      "crewCompanyName": "Kowalski Remonty",
      "crewSlug": "kowalski-remonty-warszawa",
      "uploadedAt": "2026-05-01T12:00:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```
Response `403`: brak roli ADMIN

### PUT /api/admin/moderation/photos/{id}
Request: `{ "decision": "APPROVE" | "REJECT", "note": "..." }`
- `note` wymagane przy `REJECT` (min. 5 znaków)
Response `200`: `PhotoResponse`
Response `403`: brak roli ADMIN
Response `404`: zdjęcie nie istnieje
Response `409`: zdjęcie już zmoderowane

### GET /api/admin/crews?page=0&size=20&blocked=
Parametr `blocked` opcjonalny: `true` — tylko zablokowane, `false` — tylko niezablokowane, brak — wszystkie
Response `200`: `PagedResponse<AdminCrewResponse>`
```json
{
  "content": [
    {
      "id": "uuid",
      "companyName": "Kowalski Remonty",
      "slug": "kowalski-remonty-warszawa",
      "city": "Warszawa",
      "voivodeship": "MAZOWIECKIE",
      "visible": true,
      "blocked": false,
      "blockReason": null,
      "avgRating": 4.5,
      "reviewCount": 12,
      "ownerEmail": "kontakt@kowalski.pl",
      "createdAt": "2026-05-01T12:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```
Response `403`: brak roli ADMIN

### PUT /api/admin/crews/{id}/block
Request: `{ "blocked": true, "reason": "Naruszenie regulaminu" }` — `reason` wymagane przy `blocked=true` (min. 5 znaków)
Response `200`: `AdminCrewResponse`
- Przy `blocked=true`: profil znika z publicznego `GET /api/crew/profiles`
Response `400`: błąd walidacji (brak reason przy blokowaniu)
Response `403`: brak roli ADMIN
Response `404`: profil nie istnieje

---

<!-- Kolejne endpointy dodawaj tutaj w miarę implementacji -->
