# Backend — Spring Boot

## Struktura pakietów

pl.ekipyremontowe/
├── config/          — Spring Security, CORS, S3, async config
├── auth/            — JWT, rejestracja, weryfikacja email
├── crew/            — profil ekipy, CRUD, wyszukiwanie
├── photo/           — upload, moderacja (SightEngine), S3 storage
├── review/          — opinie klientów
├── payment/         — integracja Przelewy24, webhooks, scheduled jobs
├── admin/           — panel admina, moderacja
├── common/          — wyjątki, DTO bazowe, utils
└── messaging/       — (placeholder na przyszły czat, nie implementuj teraz)

## Wzorce

- Warstwa: Controller → Service → Repository. Controller NIGDY nie zawiera logiki biznesowej.
- DTO: osobne klasy Request/Response w podpakiecie dto/. Nigdy nie zwracaj encji JPA bezpośrednio.
- Wyjątki: GlobalExceptionHandler z @RestControllerAdvice. Custom exceptions dziedziczą z RuntimeException.
- Paginacja: zawsze używaj Spring Data Pageable. Domyślny rozmiar strony: 20.

## Testy

- Testy jednostkowe: JUnit 5 + Mockito. Mockuj repozytoria w testach serwisów.
- Testy integracyjne: @SpringBootTest + Testcontainers (PostgreSQL).
- Nazewnictwo: `should_returnExpectedResult_when_condition()`
- Testy integracyjne w `src/test/java/.../integration/`.

## Styl kodu

- Final na polach serwisów (constructor injection, nie @Autowired na polach)
- Records dla DTO (Java records, nie klasy z getterami)
- Optional zamiast null. Nigdy nie zwracaj null z serwisów.
- Logowanie: SLF4J (@Slf4j lombok). ERROR/WARN/INFO/DEBUG.
