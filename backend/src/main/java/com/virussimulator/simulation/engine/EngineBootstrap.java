package com.virussimulator.simulation.engine;

import com.virussimulator.simulation.model.SimulationState;

public record EngineBootstrap(SimulationState state, SeededRandom rng) {
}
