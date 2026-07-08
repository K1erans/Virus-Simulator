package com.virussimulator.simulation.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SimulationSnapshot(
		int day,
		int S,
		int E,
		int I,
		int R,
		int deaths,
		List<Agent> cells) {
}
