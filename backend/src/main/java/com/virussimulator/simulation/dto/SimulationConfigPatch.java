package com.virussimulator.simulation.dto;

import com.virussimulator.simulation.model.SimulationConfig;

public record SimulationConfigPatch(
		Integer populationSize,
		Double initialInfectedPct,
		Double transmissionRate,
		Integer incubationPeriod,
		Integer infectiousPeriod,
		Double recoveryRate,
		Long randomSeed,
		String modelType,
		Integer maxDays) {

	public SimulationConfig mergeInto(SimulationConfig base) {
		return new SimulationConfig(
				populationSize != null ? populationSize : base.populationSize(),
				initialInfectedPct != null ? initialInfectedPct : base.initialInfectedPct(),
				transmissionRate != null ? transmissionRate : base.transmissionRate(),
				incubationPeriod != null ? incubationPeriod : base.incubationPeriod(),
				infectiousPeriod != null ? infectiousPeriod : base.infectiousPeriod(),
				recoveryRate != null ? recoveryRate : base.recoveryRate(),
				randomSeed != null ? randomSeed : base.randomSeed(),
				modelType != null ? modelType : base.modelType(),
				maxDays != null ? maxDays : base.maxDays());
	}
}
