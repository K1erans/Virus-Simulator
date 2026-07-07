import type {
	Agent,
	SimulationConfig,
	SimulationEvent,
	SimulationState,
	SimulationStats,
	TransmissionLink
} from '$lib/types/simulation';

// User-pickable transmission radius; default to 0.12 if not overridden in configuration
export const DEFAULT_TRANSMISSION_RADIUS = 0.12;
export let TRANSMISSION_RADIUS = DEFAULT_TRANSMISSION_RADIUS;
// (You can wire this to a UI selection bar; update TRANSMISSION_RADIUS when changed.)

export type DayStepRuntime = {
	firstTransmissionLogged: boolean;
};

type Counts = {
	S: number;
	E: number;
	I: number;
	R: number;
};

function distance(a: Agent, b: Agent): number {
	const dx = a.x - b.x;
	const dy = a.y - b.y;
	return Math.sqrt(dx * dx + dy * dy);
}

export function setInfectionState(
	agent: Agent,
	state: 'E' | 'I',
	config: SimulationConfig,
	rng: () => number,
	partial = false
) {
	agent.state = state;
	if (state === 'E') {
		agent.exposedDays = partial ? Math.floor(rng() * config.incubationPeriod) : 0;
		agent.infectiousDays = 0;
	} else {
		agent.infectiousDays = partial ? Math.floor(rng() * config.infectiousPeriod) : 0;
		agent.exposedDays = 0;
	}
}

export function spreadInfections(
	state: SimulationState,
	config: SimulationConfig,
	rng: () => number,
	day: number,
	runtime: DayStepRuntime
): { newInfections: number; links: TransmissionLink[]; events: SimulationEvent[] } {
	let newInfections = 0;
	const links: TransmissionLink[] = [];
	const events: SimulationEvent[] = [];

	for (let i = 0; i < state.agents.length; i++) {
		const source = state.agents[i];
		if (source.state !== 'I') continue;

		for (let j = 0; j < state.agents.length; j++) {
			if (i === j) continue;

			const target = state.agents[j];
			if (target.state !== 'S') continue;

			const dist = distance(source, target);
			if (dist > TRANSMISSION_RADIUS) continue;

			const prob = config.transmissionRate * (1 - dist / TRANSMISSION_RADIUS);
			if (rng() >= prob) continue;

			setInfectionState(target, 'E', config, rng);
			newInfections++;
			links.push({ from: i, to: j });

			if (!runtime.firstTransmissionLogged) {
				runtime.firstTransmissionLogged = true;
				events.push({ day, message: 'First transmission occurred' });
			}
		}
	}

	return { newInfections, links, events };
}

export function advanceAgents(agents: Agent[], config: SimulationConfig, rng: () => number) {
	for (const agent of agents) {
		if (agent.state === 'E') {
			agent.exposedDays++;
			if (agent.exposedDays >= config.incubationPeriod) {
				setInfectionState(agent, 'I', config, rng);
			}
		} else if (agent.state === 'I') {
			agent.infectiousDays++;
			if (agent.infectiousDays >= config.infectiousPeriod || rng() < config.recoveryRate) {
				agent.state = 'R';
			}
		}
	}
}

export function moveAgents(agents: Agent[], rng: () => number) {
	for (const agent of agents) {
		if (agent.state === 'S' || agent.state === 'R') {
			agent.x = Math.max(0.02, Math.min(0.98, agent.x + (rng() - 0.5) * 0.02));
			agent.y = Math.max(0.02, Math.min(0.98, agent.y + (rng() - 0.5) * 0.02));
		}
	}
}

export function countStates(agents: Agent[]): Counts {
	let S = 0;
	let E = 0;
	let I = 0;
	let R = 0;
	for (const agent of agents) {
		if (agent.state === 'S') S++;
		else if (agent.state === 'E') E++;
		else if (agent.state === 'I') I++;
		else R++;
	}
	return { S, E, I, R };
}

export function computeStats(
	snapshot: { S: number; E: number; I: number; R: number },
	config: SimulationConfig,
	newInfections: number,
	prevPeak: number
): SimulationStats {
	const totalInfected = snapshot.E + snapshot.I + snapshot.R;
	const attackRate = (totalInfected / config.populationSize) * 100;
	const activeCases = snapshot.E + snapshot.I;
	const recoveryRatePct = totalInfected > 0 ? (snapshot.R / totalInfected) * 100 : 0;

	return {
		peakInfectious: Math.max(prevPeak, snapshot.I),
		peakInfectiousDay: 0,
		totalInfected,
		attackRate,
		r0: config.transmissionRate * config.infectiousPeriod,
		newInfections,
		activeCases,
		recoveryRatePct
	};
}
