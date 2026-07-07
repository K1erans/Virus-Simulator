package com.virussimulator.simulation.model;

public record SimulationConfig(
		int populationSize,
		double initialInfectedPct,
		double transmissionRate,
		int incubationPeriod,
		int infectiousPeriod,
		double recoveryRate,
		long randomSeed,
		String modelType,
		int maxDays) {
}
