# Frontend — React + TypeScript

## Struktura

```
src/
├── api/             — klient Axios, interceptory, typy DTO
├── components/      — reusable komponenty (Button, Card, Rating, PhotoUpload...)
├── pages/           — strony routera (HomePage, CrewListPage, LoginPage...)
├── hooks/           — custom hooks (useAuth, useCrews, usePhotos...)
├── context/         — AuthContext (JWT, user state)
├── types/           — współdzielone typy TypeScript
└── utils/           — helpery (formatDate, formatPrice, slugify...)
```

## Reguły

- Functional components + hooks. Żadnych class components.
- TypeScript strict mode. Żadnych `any`. Każdy props ma interfejs.
- Stylowanie: Tailwind CSS v4 (klasy utility). Żadnych plików .css oprócz globals.
- Fetching: custom hooks opakowujące Axios. Nie wywołuj fetch() bezpośrednio w komponentach.
- Formularze: React Hook Form + zod do walidacji.
- Stany ładowania i błędów: każdy hook zwraca `{ data, isLoading, error }`.
- Responsywność: mobile-first. Ekipy remontowe używają telefonów.

## Wzorce plików

- Komponent: `ComponentName.tsx` (PascalCase)
- Hook: `useNazwa.ts` (camelCase z prefixem use)
- Typ/interfejs: `nazwa.types.ts`
- Strona: `NazwaPage.tsx`

## Design system

Tokeny zdefiniowane w `src/index.css` w bloku `@theme {}`. Używaj ich zamiast domyślnych klas Tailwinda.

| Token | Użycie |
|---|---|
| `bg-brand-500` / `hover:bg-brand-600` | Przyciski CTA, akcenty (pomarańczowy #f97316) |
| `text-navy-900` / `text-navy-800` | Nagłówki, labele |
| `text-navy-600` | Tekst pomocniczy |
| `text-muted` | Placeholdery, drobny tekst (#8ba0b4) |
| `bg-surface` | Tło strony (ciepły off-white #f5f4f1) |
| `bg-surface-card` | Tło kart/formularzy (biały) |
| `border-navy-100` | Obramowania inputów i kart |

Font: **Inter** (Google Fonts, załadowany w `index.html`).
Styl kart: `rounded-xl border border-navy-100 shadow-sm` — subtelny, nie przesadzony.
Przyciski: `rounded-lg` (8px).

Przy nowych stronach/komponentach — używaj `/frontend-design` skill dla niegenierycznego UI.

## Biblioteki UI

**Radix UI** — headless prymitywy, instalowane selektywnie przy potrzebie:
- `@radix-ui/react-dialog` — modale (Sprint 3: podgląd zdjęcia)
- `@radix-ui/react-select` — dropdowny (Sprint 2: województwo, kategoria)
- `@radix-ui/react-toast` — notyfikacje (Sprint 4: po dodaniu opinii)
- `@radix-ui/react-tooltip` — tooltips (Sprint 6: panel admina)

**Nie używamy** shadcn/ui (konflikt z własnym design systemem) ani TailwindUI (płatne).
