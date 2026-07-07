package com.virussimulator.simulation.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.virussimulator.simulation.model.SimulationConfig;

class SimulationEngineTest {

	private static final SimulationConfig CONFIG = new SimulationConfig(
			12,
			25.0,
			0.25,
			3,
			7,
			0.14,
			42,
			"Hybrid (Agent + SEIR)",
			100);

	@Test
	void createEngineIsDeterministic() {
		var first = SimulationEngine.createEngine(CONFIG);
		var second = SimulationEngine.createEngine(CONFIG);

		assertThat(first.getAgents()).isEqualTo(second.getAgents());
		assertThat(first.getSnapshot()).isEqualTo(second.getSnapshot());
		assertThat(first.getStats()).isEqualTo(second.getStats());
		assertThat(first.getEvents()).isEqualTo(second.getEvents());
		assertThat(first.getSnapshot().S()).isEqualTo(9);
		assertThat(first.getSnapshot().E()).isEqualTo(2);
		assertThat(first.getSnapshot().I()).isEqualTo(1);
		assertThat(first.getSnapshot().R()).isZero();
	}

	@Test
	void tickEngineIsDeterministicWithSameSeed() {
		var first = SimulationEngine.createEngine(CONFIG);
		var second = SimulationEngine.createEngine(CONFIG);
		var firstRng = new SeededRandom(999);
		var secondRng = new SeededRandom(999);

		SimulationEngine.tickEngine(first, CONFIG, firstRng);
		SimulationEngine.tickEngine(second, CONFIG, secondRng);

		assertThat(first.getAgents()).isEqualTo(second.getAgents());
		assertThat(first.getSnapshot()).isEqualTo(second.getSnapshot());
		assertThat(first.getHistory()).isEqualTo(second.getHistory());
		assertThat(first.getEvents()).isEqualTo(second.getEvents());
		assertThat(first.getStats()).isEqualTo(second.getStats());
		assertThat(first.getLinks()).isEqualTo(second.getLinks());
		assertThat(first.getSnapshot().day()).isEqualTo(1);
	}

	@Test
	void statsUseTransmissionRateTimesInfectiousPeriodForR0() {
		var engine = SimulationEngine.createEngine(CONFIG);

		assertThat(engine.getStats().r0()).isEqualTo(1.75);
	}

	@Test
	void seededRandomMatchesTypescriptLcg() {
		var rng = new SeededRandom(42);

		assertThat(rng.nextDouble()).isEqualTo(705893.0 / 2147483646.0);
	}
}
