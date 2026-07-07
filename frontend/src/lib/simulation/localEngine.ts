import type { SimulationEngine } from '$lib/simulation/engine';
import { createEngine, makeRng, tickEngine } from '$lib/simulation/mockEngine';
import type { SimulationConfig, SimulationState } from '$lib/types/simulation';

const TICK_MS = 200;

export function createLocalEngine(initialConfig: SimulationConfig): SimulationEngine {
	let state = createEngine(initialConfig);
	let rng = makeRng(initialConfig.randomSeed);
	let runtime = { firstTransmissionLogged: false };
	let intervalId: ReturnType<typeof setInterval> | null = null;
	const listeners = new Set<(state: SimulationState) => void>();

	function notify() {
		for (const listener of listeners) {
			listener(state);
		}
	}

	function clearTickInterval() {
		if (intervalId !== null) {
			clearInterval(intervalId);
			intervalId = null;
		}
	}

	async function pause() {
		clearTickInterval();
	}

	function doTick() {
		if (state.snapshot.day >= initialConfig.maxDays) {
			void pause();
			state.events = [
				...state.events,
				{
					day: state.snapshot.day,
					message: `Simulation complete (${initialConfig.maxDays} days)`
				}
			];
			notify();
			return;
		}

		tickEngine(state, initialConfig, rng, runtime);
		notify();
	}

	return {
		async start(config) {
			initialConfig = { ...config };

			if (state.snapshot.day === 0 && state.events.length <= 1) {
				state.events = [...state.events, { day: 0, message: 'Simulation started' }];
				notify();
			}

			clearTickInterval();
			intervalId = setInterval(doTick, TICK_MS);
		},

		pause,

		async reset(config) {
			await pause();
			initialConfig = { ...config };
			state = createEngine(initialConfig);
			rng = makeRng(initialConfig.randomSeed);
			runtime = { firstTransmissionLogged: false };
			notify();
		},

		destroy() {
			clearTickInterval();
			listeners.clear();
		},

		subscribe(listener) {
			listeners.add(listener);
			listener(state);
			return () => listeners.delete(listener);
		}
	};
}
