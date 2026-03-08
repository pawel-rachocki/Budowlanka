# Frontend — React + TypeScript

## Struktura

src/
├── api/             — klient Axios, interceptory, typy DTO
├── components/      — reusable komponenty (Button, Card, Rating, PhotoUpload...)
├── pages/           — strony routera (HomePage, CrewListPage, CrewProfilePage, LoginPage...)
├── hooks/           — custom hooks (useAuth, useCrews, usePhotos...)
├── context/         — AuthContext (JWT, user state)
├── types/           — współdzielone typy TypeScript
└── utils/           — helpery (formatDate, formatPrice, slugify...)

## Reguły

- Functional components + hooks. Żadnych class components.
- TypeScript strict mode. Żadnych `any`. Każdy props ma interfejs.
- Stylowanie: Tailwind CSS (klasy utility). Żadnych plików .css oprócz globals.
- Fetching: custom hooks opakowujące Axios. Nie fetch() bezpośrednio w komponentach.
- Formularze: React Hook Form + zod do walidacji.
- Stany ładowania i błędów: każdy hook zwraca { data, isLoading, error }.
- Responsywność: mobile-first. Ekipy remontowe używają telefonów.

## Wzorce plików

- Komponent: `ComponentName.tsx` (PascalCase)
- Hook: `useNazwa.ts` (camelCase z prefixem use)
- Typ/interfejs: `nazwa.types.ts`
- Strona: `NazwaPage.tsx`
