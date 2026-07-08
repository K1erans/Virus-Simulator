import type { Agent, TransmissionLink } from './agent';
import type { SimulationEvent } from './event';
import type { HistoryPoint, SimulationSnapshot } from './snapshot';
import type { SimulationStats } from './stats';

export type DayDelta = {
	day: number;
	S: number;
	E: number;
	I: number;
	R: number;
	deaths: number;
};

export type SnapshotPayload = {
	agents: Agent[];
	snapshot: SimulationSnapshot;
	history?: HistoryPoint[] | null;
	days?: DayDelta[] | null;
	events: SimulationEvent[];
	stats: SimulationStats;
	links: TransmissionLink[];
};

export type LiveMessage = { type: 'live' } & SnapshotPayload;
export type BatchMessage = { type: 'batch' } & SnapshotPayload;
export type CompleteMessage = { type: 'complete'; message?: string } & SnapshotPayload;

export type SnapshotMessage =
	| LiveMessage
	| BatchMessage
	| CompleteMessage
	| { type: 'connected'; message: string }
	| { type: 'error'; message: string };

export type SimulationStatusResponse = {
	status: string;
	running: boolean;
};
