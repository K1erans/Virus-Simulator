export interface SimulationStats {
	peakInfectious: number;
	peakInfectiousDay: number;
	totalInfected: number;
	attackRate: number;
	r0: number;
	newInfections: number;
	activeCases: number;
	recoveryRatePct: number;
}
