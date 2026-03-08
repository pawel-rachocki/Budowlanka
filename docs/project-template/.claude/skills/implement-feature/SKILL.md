---
name: implement-feature
description: "Implementacja nowego feature'a od specyfikacji po testy. Użyj gdy rozpoczynasz nowy task z SPEC.md lub gdy ktoś poda opis feature'a do zbudowania."
---

Workflow implementacji feature'a:

1. **Przeczytaj kontekst**: Otwórz @SPEC.md i znajdź odpowiedni sprint/task.
   Otwórz @docs/api-contracts.md i @docs/database-schema.sql jeśli dotyczy.

2. **Zaplanuj**: Przed napisaniem kodu, wypisz:
   - Jakie pliki trzeba stworzyć/zmodyfikować
   - Jakie migracje Flyway potrzebne
   - Jakie endpointy (metoda, URL, request/response DTO)
   - Jakie testy napisać

3. **Implementuj warstwami** (w tej kolejności):
   a. Migracja Flyway (jeśli zmiana DB)
   b. Entity JPA
   c. Repository
   d. Service (+ unit test)
   e. Controller (+ DTO request/response)
   f. Test integracyjny (jeśli krytyczny flow)
   g. Frontend: typy DTO → hook API → komponent/strona

4. **Zweryfikuj**: Po implementacji uruchom:
   - `cd backend && ./mvnw test`
   - `cd frontend && npx tsc --noEmit && npm run lint`

5. **Zaktualizuj docs**: Dodaj/zaktualizuj kontrakt w @docs/api-contracts.md
