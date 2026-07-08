import { USE_MOCK } from '$lib/api/client';
import type { SimulationEngine } from '$lib/simulation/engine';
import { createLocalEngine } from '$lib/simulation/localEngine';
import { createRemoteEngine } from '$lib/simulation/remoteEngine';
import type { SimulationConfig, ViewOptions } from '$lib/types/config';
import type { SimulationState } from '$lib/types/engine';

const defaultConfig: SimulationConfig = {
	populationSize: 1000,
	initialInfectedPct: 1.0,
	transmissionRate: 0.25,
	incubationPeriod: 3,
	infectiousPeriod: 7,
	recoveryRate: 0.14,
	randomSeed: 42,
	modelType: 'Hybrid (Agent + SEIR)',
	maxDays: 100
};

const defaultViewOptions: ViewOptions = {
	agentView: true,
	seirCurves: true,
	populationStats: true,
	transmissionNetwork: false
};

type EngineSource = 'local' | 'backend';
type EngineStatus = {
	connected: boolean;
	error?: string | null;
};

function updateStatus(status: EngineStatus) {
	simulation.connected = status.connected;

	if ('error' in status) {
		simulation.error = status.error ?? null;
	} else if (status.connected) {
		// Clear stale errors on reconnect.
		simulation.error = null;
	}

	if (!status.connected) {
		simulation.running = false;
	}
}

const engine: SimulationEngine = USE_MOCK
	? createLocalEngine(defaultConfig)
	: createRemoteEngine(defaultConfig, updateStatus);

let initialState: SimulationState | undefined;
let ready = false;

function syncFromEngine(state: SimulationState) {
	if (!ready) {
		initialState = state;
		return;
	}

	simulation.agents = [...state.agents];
	simulation.snapshot = { ...state.snapshot };
	simulation.history = [...state.history];
	simulation.events = [...state.events];
	simulation.stats = { ...state.stats };
	simulation.links = [...state.links];

	if (state.snapshot.day >= simulation.config.maxDays) {
		simulation.running = false;
	}
}

engine.subscribe(syncFromEngine);

if (!initialState) {
	throw new Error('Simulation engine did not provide initial state');
}

export const simulation = $state({
	config: { ...defaultConfig },
	agents: initialState.agents,
	snapshot: initialState.snapshot,
	history: initialState.history,
	events: initialState.events,
	stats: initialState.stats,
	links: initialState.links,
	running: false,
	viewOptions: { ...defaultViewOptions },
	connected: USE_MOCK,
	error: null as string | null,
	loading: false,
	source: (USE_MOCK ? 'local' : 'backend') as EngineSource
});

ready = true;

function configSnapshot(): SimulationConfig {
	return $state.snapshot(simulation.config);
}

function errorMessage(error: unknown) {
	return error instanceof Error ? error.message : String(error);
}

export async function start() {
	if (simulation.running || simulation.loading) return;

	simulation.loading = true;
	simulation.error = null;

	try {
		await engine.start(configSnapshot());
		simulation.running = true;
	} catch (error) {
		simulation.error = errorMessage(error);
		simulation.running = false;
	} finally {
		simulation.loading = false;
	}
}

export async function pause() {
	if (simulation.loading) return;

	simulation.loading = true;
	simulation.error = null;

	try {
		await engine.pause();
		simulation.running = false;
	} catch (error) {
		simulation.error = errorMessage(error);
	} finally {
		simulation.loading = false;
	}
}

export async function reset() {
	if (simulation.loading) return;

	simulation.loading = true;
	simulation.error = null;

	try {
		await engine.reset(configSnapshot());
		simulation.running = false;
	} catch (error) {
		simulation.error = errorMessage(error);
	} finally {
		simulation.loading = false;
	}
}
