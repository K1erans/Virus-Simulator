import type { SimulationConfig, SimulationState } from '$lib/types/simulation';

export interface SimulationEngine {
	start(config: SimulationConfig): Promise<void>;
	pause(): Promise<void>;
	reset(config: SimulationConfig): Promise<void>;
	destroy(): void;
	subscribe(listener: (state: SimulationState) => void): () => void;
}
