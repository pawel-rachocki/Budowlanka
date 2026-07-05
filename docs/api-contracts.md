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

## Packages — `/api/packages`

Publiczny katalog (cennik) pakietów. Zwracane są tylko pakiety aktywne (`is_active=true`), posortowane rosnąco po cenie.

### GET /api/packages/listing
Auth: brak (publiczny)
Response `200`: `List<ListingPackageResponse>`
```json
[
  { "id": "uuid", "name": "7 dni", "durationDays": 7, "pricePln": 29.00 },
  { "id": "uuid", "name": "14 dni", "durationDays": 14, "pricePln": 49.00 },
  { "id": "uuid", "name": "30 dni", "durationDays": 30, "pricePln": 89.00 },
  { "id": "uuid", "name": "365 dni", "durationDays": 365, "pricePln": 699.00 }
]
```

### GET /api/packages/boost
Auth: brak (publiczny)
Response `200`: `List<BoostPackageResponse>`
```json
[
  { "id": "uuid", "name": "Boost 7 dni", "durationDays": 7, "pricePln": 19.00 },
  { "id": "uuid", "name": "Boost 30 dni", "durationDays": 30, "pricePln": 49.00 }
]
```

---

## Payments — `/api/payments`

Auth: `Bearer {accessToken}` (rola CREW) — wszystkie endpointy. Inicjacja tworzy rekord `payments` w stanie `PENDING` i rejestruje transakcję w Przelewy24. Aktywacja pakietu/boosta następuje dopiero po zaksięgowaniu płatności (webhook), nie w tych endpointach.

`PaymentResponse`:
```json
{
  "id": "uuid",
  "amountPln": 89.00,
  "currency": "PLN",
  "paymentType": "LISTING",
  "status": "PENDING",
  "providerTxId": null,
  "createdAt": "2026-07-04T12:00:00Z",
  "completedAt": null
}
```
- `paymentType`: `LISTING` lub `BOOST`
- `status`: `PENDING` | `COMPLETED` | `FAILED` | `REFUNDED`
- `providerTxId`: identyfikator transakcji u operatora — `null` dopóki płatność nie zaksięgowana
- `completedAt`: `null` dopóki status nie `COMPLETED`

### POST /api/payments/listing
Auth: `Bearer {accessToken}` (rola CREW)
Request:
```json
{ "packageId": "uuid" }
```
- `packageId`: wymagany, UUID pakietu z katalogu `listing_packages` (aktywnego)

Response `200`:
```json
{ "redirectUrl": "https://sandbox.przelewy24.pl/trnRequest/{token}" }
```
Response `400`: błąd walidacji (brak `packageId`)
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `404`: pakiet nie istnieje lub jest nieaktywny; profil ekipy nie istnieje
Response `502`: błąd komunikacji z bramką Przelewy24

### POST /api/payments/boost
Auth: `Bearer {accessToken}` (rola CREW)
Request:
```json
{ "boostPackageId": "uuid" }
```
- `boostPackageId`: wymagany, UUID pakietu z katalogu `boost_packages` (aktywnego)

Response `200`:
```json
{ "redirectUrl": "https://sandbox.przelewy24.pl/trnRequest/{token}" }
```
Response `400`: błąd walidacji (brak `boostPackageId`)
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `404`: pakiet nie istnieje lub jest nieaktywny; profil ekipy nie istnieje
Response `502`: błąd komunikacji z bramką Przelewy24

### GET /api/payments/my
Auth: `Bearer {accessToken}` (rola CREW)
Response `200`: `List<PaymentResponse>` — wszystkie płatności ekipy, posortowane od najnowszych
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `404`: profil ekipy nie istnieje

### POST /api/payments/webhook/p24
Auth: brak (publiczny — uwierzytelnienie przez podpis P24, nie JWT). Endpoint notyfikacji serwer-serwer wywoływany przez Przelewy24 (`urlStatus`).
Content-Type: `application/json`
Request (`P24WebhookNotification`):
```json
{
  "merchantId": 12345,
  "posId": 12345,
  "sessionId": "3f1c...uuid płatności",
  "amount": 8900,
  "originAmount": 8900,
  "currency": "PLN",
  "orderId": 987654321,
  "methodId": 25,
  "statement": "platnosc",
  "sign": "sha384hex..."
}
```
- `sessionId`: nasze `payments.id` (UUID) nadane przy inicjacji płatności
- `amount` / `originAmount`: kwota w groszach (int)
- `orderId`: identyfikator transakcji nadany przez P24 → zapisywany jako `provider_tx_id`
- `sign`: podpis SHA384 liczony z `{merchantId, posId, sessionId, amount, originAmount, currency, orderId, methodId, statement, crc}`

Przetwarzanie:
- Weryfikacja podpisu (`P24SignatureUtil`). Niezgodny → `400`, brak akcji.
- Idempotentność: płatność szukana po `sessionId`; gdy już `COMPLETED` → `200` bez ponownej akcji.
- Potwierdzenie u P24 (`verifyTransaction`), następnie aktywacja pakietu (`SubscriptionActivationService`) i `status=COMPLETED`, `provider_tx_id=orderId`, `completed_at=now`.

Response `400`: nieprawidłowy podpis (żądanie odrzucone)
Response `200`: notyfikacja przyjęta — **zawsze** po pomyślnej weryfikacji podpisu (także przy błędach biznesowych: nieznany `sessionId`, niezgodna kwota, verify=fail, błąd bramki), by P24 nie ponawiał w nieskończoność. Szczegóły w logach.

---

## Subscription — `/api/crew/subscription`

Status subskrypcji i boosta zalogowanej ekipy — dane dla dashboardu ekipy (E-06).

### GET /api/crew/subscription/me
Auth: `Bearer {accessToken}` (rola CREW)
Response `200`: `SubscriptionStatusResponse`
```json
{
  "hasActiveSubscription": true,
  "isVisible": true,
  "subscription": { "packageName": "30 dni", "expiresAt": "2026-08-04T12:00:00Z", "active": true },
  "boost": { "boostName": "Boost 7 dni", "expiresAt": "2026-07-12T12:00:00Z" }
}
```
- `hasActiveSubscription`: `true` gdy istnieje aktywna subskrypcja (`is_active=true` i `expires_at > NOW()`)
- `isVisible`: aktualna flaga `is_visible` profilu ekipy
- `subscription`: aktywna subskrypcja (`packageName`, `expiresAt`, `active`) lub `null` gdy brak
- `boost`: aktywny boost (`boostName`, `expiresAt`) lub `null` gdy brak

Brak aktywnej subskrypcji zwraca `200` z obiektem „pustym" (front pokazuje CTA „Wykup pakiet"), **nie** `404`:
```json
{ "hasActiveSubscription": false, "isVisible": false, "subscription": null, "boost": null }
```
Response `401`: brak lub nieprawidłowy token
Response `403`: zalogowany użytkownik nie ma roli CREW
Response `404`: profil ekipy nie istnieje

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

## Reviews — `/api/crew/profiles/{slug}/reviews`

`ReviewResponse`:
```json
{
  "id": "uuid",
  "rating": 4,
  "comment": "Świetna robota, polecam!",
  "authorDisplayName": "jan***",
  "authorUserId": "uuid",
  "createdAt": "2026-05-20T10:00:00Z"
}
```
- `authorDisplayName`: zamaskowany prefiks emaila autora (np. `jan***`) — pełny email nie jest ujawniany publicznie (RODO).
- `authorUserId`: UUID autora — frontend porównuje z ID zalogowanego użytkownika, by pokazać przyciski Edytuj/Usuń.

### GET /api/crew/profiles/{slug}/reviews?page=0&size=20
Auth: brak (publiczny)
Response `200`: `PagedResponse<ReviewResponse>`
```json
{
  "content": [ { "id": "uuid", "rating": 4, "comment": "Świetna robota!", "authorDisplayName": "jan***", "authorUserId": "uuid", "createdAt": "2026-05-20T10:00:00Z" } ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```
Response `404`: profil nie istnieje lub jest ukryty/zablokowany

### POST /api/crew/profiles/{slug}/reviews
Auth: `Bearer {accessToken}` (rola CLIENT)
Request:
```json
{ "rating": 4, "comment": "Bardzo solidna ekipa, polecam." }
```
- `rating`: wymagany, liczba całkowita 1–5
- `comment`: opcjonalny, 10–1000 znaków, nie może składać się wyłącznie ze spacji

Response `201`: `ReviewResponse`
Response `400`: błąd walidacji
Response `401`: brak lub nieprawidłowy token
Response `403`: brak roli CLIENT
Response `404`: profil nie istnieje lub jest ukryty/zablokowany
Response `409`: użytkownik już wystawił opinię tej ekipie

### PUT /api/crew/profiles/{slug}/reviews/{reviewId}
Auth: `Bearer {accessToken}` (rola CLIENT, tylko właściciel opinii)
Request:
```json
{ "rating": 5, "comment": "Po zastanowieniu — zasługują na 5 gwiazdek." }
```
Response `200`: `ReviewResponse`
Response `400`: błąd walidacji
Response `401`: brak lub nieprawidłowy token
Response `403`: opinia nie należy do zalogowanego użytkownika
Response `404`: opinia nie istnieje

### DELETE /api/crew/profiles/{slug}/reviews/{reviewId}
Auth: `Bearer {accessToken}` (rola CLIENT, tylko właściciel opinii)
Response `204`
Response `401`: brak lub nieprawidłowy token
Response `403`: opinia nie należy do zalogowanego użytkownika
Response `404`: opinia nie istnieje

---

<!-- Kolejne endpointy dodawaj tutaj w miarę implementacji -->
