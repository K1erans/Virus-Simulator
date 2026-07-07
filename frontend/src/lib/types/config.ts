export interface SimulationConfig {
	populationSize: number;
	initialInfectedPct: number;
	transmissionRate: number;
	incubationPeriod: number;
	infectiousPeriod: number;
	recoveryRate: number;
	randomSeed: number;
	modelType: string;
	maxDays: number;
}

export interface ViewOptions {
	agentView: boolean;
	seirCurves: boolean;
	populationStats: boolean;
	transmissionNetwork: boolean;
}
