package com.virussimulator.simulation.model;

public record SimulationStats(
		int peakInfectious,
		int peakInfectiousDay,
		int totalInfected,
		double attackRate,
		double r0,
		int newInfections,
		int activeCases,
		double recoveryRatePct) {
}
