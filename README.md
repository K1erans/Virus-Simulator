# Virus Simulator

Hybrid agent + SIR virus simulation with a **SvelteKit frontend** and **Spring Boot backend**.

## Structure

```
Virus-Simulator/
├── backend/    Spring Boot 3.5 (Java 24) — simulation logic + WebSocket
└── frontend/   SvelteKit 2 + Svelte 5 — visualization + controls
```

## Run locally

From the repo root (backend on 8080, frontend on 5173):

```bash
npm install
cd frontend && npm install && cd ..
npm run dev
```

Or run each service in its own terminal:

```bash
cd backend && ./gradlew bootRun
```

```bash
cd frontend && npm run dev
```

Open [http://localhost:5173](http://localhost:5173). The home page checks both links:

- `GET /api/health` — REST API via Vite proxy
- `WS /ws/simulation` — WebSocket via Vite proxy

## API (stubs ready for you)

| Method | Path | Status |
|--------|------|--------|
| GET | `/api/health` | Working |
| POST | `/api/simulation/start` | 501 — implement |
| POST | `/api/simulation/pause` | 501 — implement |
| POST | `/api/simulation/reset` | 501 — implement |
| GET | `/api/simulation/config` | 501 — implement |
| WS | `/ws/simulation` | Connected — implement tick streaming |

## Implementation guide

**Backend** — add packages under `com.virussimulator`:

- `model/` — `Agent`, `AgentState`, `SimulationConfig`, `SimulationSnapshot`
- `simulation/` — `SimulationEngine`, `SirTracker`, `SimulationService`

**Frontend** — add under `src/lib/`:

- `components/AgentCanvas.svelte` — canvas rendering
- `components/SirChart.svelte` — S/I/R line chart
- `components/ControlPanel.svelte` — parameters + buttons
- Extend `api/client.ts` with simulation REST helpers

## Dev notes

- CORS allows `http://localhost:5173` on the backend
- Vite proxies `/api` and `/ws` to `localhost:8080` so the frontend uses relative URLs in dev
