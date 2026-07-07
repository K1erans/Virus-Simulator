package com.virussimulator.simulation.model;

import java.util.List;

public record SimulationSnapshot(
		int day,
		int S,
		int E,
		int I,
		int R,
		int deaths,
		List<Agent> cells) {
}
