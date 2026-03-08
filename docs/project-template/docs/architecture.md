# Architektura — Portal Ekipy Remontowe

## Decyzje architektoniczne

| Data | Decyzja | Uzasadnienie | Alternatywy rozważane |
|------|---------|--------------|----------------------|
| TBD  | Monorepo (backend + frontend) | Claude Code widzi cały kontekst, prostszy CI | Osobne repo |
| TBD  | JWT (access + refresh) | Stateless, prostsze dla MVP | Session-based auth |
| TBD  | Flyway (nie Liquibase) | Prostsze, SQL-native, wystarczające dla projektu | Liquibase |
| TBD  | Przelewy24 | Standard rynku PL, BLIK, najlepsza rozpoznawalność | tpay, Stripe |
| TBD  | SightEngine | Darmowy tier na start, prosty REST, dobre accuracy | Google Vision, Moderatecontent |
| TBD  | MinIO (dev) → Cloudflare R2 (prod) | S3-compatible, R2 = zero egress fees | AWS S3 |

## Diagramy

TODO: dodaj diagramy komponentów po Sprint 1

## Technologie i wersje

- Java: 21 (LTS)
- Spring Boot: 3.x (najnowszy stable)
- PostgreSQL: 16
- React: 18+
- TypeScript: 5.x (strict mode)
- Node.js: 20+ (LTS)
