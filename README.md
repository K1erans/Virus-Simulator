# Virus Simulator

Hybrid agent + SEIR virus simulation with a **SvelteKit frontend** and **Spring Boot backend**.

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

Open [http://localhost:5173](http://localhost:5173).

By default the dashboard runs the in-browser TypeScript engine. To ingest data from the Java backend, start both services and export the Vite flag (a bare assignment is not inherited by `npm run dev`):

```bash
export VITE_USE_MOCK=false
cd frontend && npm run dev
```

Or put `VITE_USE_MOCK=false` in `frontend/.env`.

## API

| Method | Path | Status |
|--------|------|--------|
| GET | `/api/health` | Working |
| POST | `/api/simulation/start` | Starts Java simulation and returns `{ status, running }` |
| POST | `/api/simulation/pause` | Pauses Java simulation and returns `{ status, running }` |
| POST | `/api/simulation/reset` | Resets Java simulation (optional config body) and returns `{ status, running }` |
| GET | `/api/simulation/config` | Returns current Java simulation config |
| GET | `/api/simulation/history` | Full history with agent cells (for scrubbing / go-back) |
| WS | `/ws/simulation` | Streams `connected`, `live`, `batch`, `complete`, and `error` messages |

Live WebSocket traffic sends batched day-count deltas (no full history). Fetch `/api/simulation/history` when the UI needs point-in-time agent cells.

## Source Layout

**Backend**:

- `backend/src/main/java/com/virussimulator/simulation/model/` — Java protocol/domain records and mutable agent state
- `backend/src/main/java/com/virussimulator/simulation/engine/` — deterministic Java engine
- `backend/src/main/java/com/virussimulator/simulation/service/` — scheduler and lifecycle module
- `backend/src/main/java/com/virussimulator/websocket/` — WebSocket sessions and snapshot broadcast

**Frontend**:

- `frontend/src/lib/simulation/engine.ts` — frontend engine interface
- `frontend/src/lib/simulation/localEngine.ts` — in-browser adapter
- `frontend/src/lib/simulation/remoteEngine.ts` — Java REST/WebSocket adapter
- `frontend/src/lib/stores/simulation.svelte.ts` — dashboard session state
- `frontend/src/lib/types/protocol.ts` — WebSocket message contract

## Test and Check

```bash
cd backend && ./gradlew test
```

```bash
cd frontend && npm test && npm run check
```

## Dev notes

- CORS allows `http://localhost:5173` on the backend
- Vite proxies `/api` and `/ws` to `localhost:8080` so the frontend uses relative URLs in dev
- `CONTEXT.md` documents the domain vocabulary, source switch, and simulation invariants
