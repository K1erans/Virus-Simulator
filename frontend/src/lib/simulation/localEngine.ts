import type { SimulationEngine } from '$lib/simulation/engine';
import { createEngine, tickEngine } from '$lib/simulation/mockEngine';
import type { SimulationConfig, SimulationState } from '$lib/types/simulation';

const TICK_MS = 200;

function configsEqual(a: SimulationConfig, b: SimulationConfig): boolean {
	return (
		a.populationSize === b.populationSize &&
		a.initialInfectedPct === b.initialInfectedPct &&
		a.transmissionRate === b.transmissionRate &&
		a.incubationPeriod === b.incubationPeriod &&
		a.infectiousPeriod === b.infectiousPeriod &&
		a.recoveryRate === b.recoveryRate &&
		a.randomSeed === b.randomSeed &&
		a.modelType === b.modelType &&
		a.maxDays === b.maxDays
	);
}

export function createLocalEngine(initialConfig: SimulationConfig): SimulationEngine {
	let bootstrap = createEngine(initialConfig);
	let state = bootstrap.state;
	let rng = bootstrap.rng;
	let runtime = { firstTransmissionLogged: false };
	let activeConfig = { ...initialConfig };
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

	function rebuild(config: SimulationConfig) {
		activeConfig = { ...config };
		bootstrap = createEngine(activeConfig);
		state = bootstrap.state;
		rng = bootstrap.rng;
		runtime = { firstTransmissionLogged: false };
	}

	function doTick() {
		if (state.snapshot.day >= activeConfig.maxDays) {
			void pause();
			const message = `Simulation complete (${activeConfig.maxDays} days)`;
			const alreadyLogged = state.events.some((event) => event.message === message);
			if (!alreadyLogged) {
				state.events = [
					...state.events,
					{
						day: state.snapshot.day,
						message
					}
				];
				notify();
			}
			return;
		}

		tickEngine(state, activeConfig, rng, runtime);
		notify();
	}

	return {
		async start(config) {
			const needsRebuild =
				state.snapshot.day >= config.maxDays || !configsEqual(config, activeConfig);

			if (needsRebuild) {
				rebuild(config);
			} else {
				activeConfig = { ...config };
			}

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
			rebuild(config);
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
