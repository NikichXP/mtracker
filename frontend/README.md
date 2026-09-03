# mtracker-ui (frontend/)

Dashboard for `mtracker-service` — a WoW Mythic+ tracker for a guild and its friends. Shows
a per-player rollup (main + alts) of Mythic+ score, weekly key count, weekly highest key,
max item level, active spec/role, and last sync time, with a drill-down into each
character's individual weekly runs, plus a "Weekly" tab for historical week-over-week
stats.

Lives in the same repository as the backend (`mtracker-service`), so both build and deploy
through a single CI pipeline on the same self-hosted runner.

## Stack

- **React 19** + **TypeScript**, built with **Vite**
- **MUI (Material Design)**: `@mui/material`, `@mui/icons-material`
- **react-router-dom** — routing (currently a single `/` route, room to grow)
- **@tanstack/react-query** — data fetching/caching against the backend (overview, player
  detail, weekly stats, available weeks), with background polling instead of manual
  `setInterval`
- `axios` for API requests

## Structure

```
src/
  api/        — axios client (client.ts) and typed backend calls (statsApi.ts, types.ts)
  hooks/      — React Query hooks on top of api/ (usePlayersOverview, usePlayerDetail, useWeeklyStats, useAvailableWeeks)
  pages/      — route-level pages (DashboardPage)
  components/ — presentational components (OverviewTab, WeeklyTab, PlayerRow, CharacterBreakdown, RoleBadge)
  utils/      — pure helpers (formatDate, role)
```

Once auth is added, `apiClient` in `api/client.ts` is the place for an `Authorization`
header interceptor and 401 handling, instead of touching every call site.

## Data

All data comes from the mtracker-service API:

- `GET /api/v1/stats/overview` — current stats per tracked player (guild members + friends)
- `GET /api/v1/stats/players/{playerKey}` — per-character breakdown for one player
- `GET /api/v1/stats/weekly?week=2026-W35` — historical stats for a given week
- `GET /api/v1/stats/weeks` — list of week keys that have data

The API base URL is set via the `VITE_API_BASE_URL` environment variable (see `.env.example`).
It defaults to an empty string (same-origin relative requests), which works in production
because the Ingress for `mtracker.nikichxp.xyz` routes the `/api` path prefix to `mtracker-service`
and everything else to `mtracker-ui` (see `infra-scripts/manifests/mtracker/30-ingress.yaml`). The
CI pipeline (`.github/workflows/frontend.yaml`) still passes it explicitly as
`https://mtracker.nikichxp.xyz` for clarity. For local dev, `vite.config.ts` proxies `/api`
to `http://localhost:8080` instead, so no env var is needed against a locally running backend.

## Development

```bash
npm install
npm run dev
```

## Build

```bash
npm run build   # tsc -b && vite build, output in dist/
```

## Docker

```bash
# build context is frontend/ itself
docker build -t mtracker-ui --build-arg VITE_API_BASE_URL=https://mtracker.nikichxp.xyz/api frontend
docker run -p 8080:8080 mtracker-ui
```

The built image is static assets served by `nginx`, listening on port `8080`, with SPA
routing (`try_files ... /index.html`).
