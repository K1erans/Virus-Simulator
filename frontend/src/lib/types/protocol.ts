import type { Agent, TransmissionLink } from './agent';
import type { SimulationEvent } from './event';
import type { HistoryPoint, SimulationSnapshot } from './snapshot';
import type { SimulationStats } from './stats';

export type SnapshotPayload = {
	agents: Agent[];
	snapshot: SimulationSnapshot;
	history: HistoryPoint[];
	events: SimulationEvent[];
	stats: SimulationStats;
	links: TransmissionLink[];
};

export type SnapshotMessage =
	| ({ type: 'snapshot' | 'complete'; message?: string } & SnapshotPayload)
	| { type: 'connected'; message: string }
	| { type: 'error'; message: string };

export type SimulationStatusResponse = {
	status: string;
	running: boolean;
};
