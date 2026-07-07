import {
	advanceAgents,
	computeStats,
	countStates,
	type DayStepRuntime,
	moveAgents,
	setInfectionState,
	spreadInfections
} from '$lib/simulation/dayStep';
import type { Agent } from '$lib/types/agent';
import type { SimulationConfig } from '$lib/types/config';
import type { SimulationState } from '$lib/types/engine';
import type { SimulationEvent } from '$lib/types/event';
import type { SimulationSnapshot } from '$lib/types/snapshot';

type EngineAgent = Agent & { id: number };
type SnapshotWithCells = SimulationSnapshot & { cells: Agent[] };

function seededRandom(seed: number) {
	let s = seed;
	return () => {
		s = (s * 16807) % 2147483647;
		return (s - 1) / 2147483646;
	};
}

export function createEngine(config: SimulationConfig): SimulationState {
	const rng = seededRandom(config.randomSeed);
	const agents: EngineAgent[] = [];

	for (let i = 0; i < config.populationSize; i++) {
		agents.push({
			id: i,
			x: rng(),
			y: rng(),
			state: 'S',
			exposedDays: 0,
			infectiousDays: 0
		});
	}

	const initialInfected = Math.max(1, Math.round(config.populationSize * (config.initialInfectedPct / 100)));
	for (let i = 0; i < initialInfected; i++) {
		setInfectionState(agents[i], i % 3 === 0 ? 'I' : 'E', config, rng, true);
	}

	const counts = countStates(agents);
	const snapshot = { day: 0, ...counts, deaths: 0 };
	const snapshotWithCells: SnapshotWithCells = { ...snapshot, cells: agents };

	return {
		agents,
		snapshot: snapshotWithCells,
		history: [snapshotWithCells],
		events: [{ day: 0, message: 'Simulation initialized' }],
		stats: computeStats(snapshot, config, 0, 0),
		links: []
	};
}

export function tickEngine(
	state: SimulationState,
	config: SimulationConfig,
	rng: () => number,
	runtime: DayStepRuntime
): { events: SimulationEvent[] } {
	const day = state.snapshot.day + 1;
	const { newInfections, links: newLinks, events } = spreadInfections(state, config, rng, day, runtime);

	advanceAgents(state.agents, config, rng);
	moveAgents(state.agents, rng);

	const counts = countStates(state.agents);
	const prevPeak = state.stats.peakInfectious;
	const snapshot = { day, ...counts, deaths: state.snapshot.deaths };
	const stats = computeStats(snapshot, config, newInfections, prevPeak);
	const snapshotWithCells: SnapshotWithCells = { ...snapshot, cells: state.agents };

	stats.peakInfectiousDay = snapshot.I > prevPeak ? day : state.stats.peakInfectiousDay;
	state.snapshot = snapshotWithCells;
	state.history.push(snapshotWithCells);
	state.links = newLinks.slice(0, 40);

	if (newInfections > 10 && day > 5) {
		events.push({ day, message: 'Rapid increase in infections' });
	}
	if (snapshot.I === 0 && snapshot.E === 0 && day > 10 && counts.R > config.populationSize * 0.5) {
		events.push({ day, message: 'Outbreak subsiding - most agents recovered' });
	}

	state.stats = stats;
	state.events.push(...events);
	return { events };
}

export function makeRng(seed: number) {
	return seededRandom(seed);
}
