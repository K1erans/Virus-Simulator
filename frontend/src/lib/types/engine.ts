import type { Agent, TransmissionLink } from './agent';
import type { SimulationEvent } from './event';
import type { HistoryPoint, SimulationSnapshot } from './snapshot';
import type { SimulationStats } from './stats';

export interface SimulationState {
	agents: Agent[];
	snapshot: SimulationSnapshot;
	history: HistoryPoint[];
	events: SimulationEvent[];
	stats: SimulationStats;
	links: TransmissionLink[];
}
