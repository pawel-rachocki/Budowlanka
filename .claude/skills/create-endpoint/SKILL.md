---
name: create-endpoint
description: "Tworzenie nowego REST API endpoint od DTO po controller. Użyj gdy
  potrzebujesz szybko dodać endpoint z pełną strukturą warstw."
---

Tworzenie endpointu — checklist:

1. **DTO** (w pakiecie `dto/` odpowiedniego modułu w `com.budowlanka`):
   - `XxxRequest` (record z @Valid, @NotBlank, @Size etc.)
   - `XxxResponse` (record, nigdy encja)

2. **Service**:
   - Metoda biznesowa, zwraca DTO response lub void
   - Rzuca custom exception przy błędach (np. ResourceNotFoundException)
   - Unit test w `src/test/java/`

3. **Controller**:
   - @RestController, @RequestMapping("/api/...")
   - @PreAuthorize dla autoryzacji roli
   - ResponseEntity z odpowiednim HTTP status (201 Created, 204 No Content etc.)
   - @Valid na @RequestBody

4. **Aktualizuj docs/api-contracts.md** z nowym endpointem

Nie zapomnij: jeśli endpoint wymaga nowej tabeli → najpierw migracja Flyway.
Po implementacji uruchom: `cd backend && mvn spotless:apply && mvn test`
