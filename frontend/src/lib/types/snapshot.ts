import type { Agent } from "./agent";
export interface SimulationSnapshot {
	day: number;
	S: number;
	E: number;
	I: number;
	R: number;
	deaths: number;
	cells: Agent[];
}

export type HistoryPoint = SimulationSnapshot;
