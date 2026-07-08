package com.virussimulator.simulation.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import com.virussimulator.simulation.model.Agent;
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
		var first = SimulationEngine.createEngine(CONFIG).state();
		var second = SimulationEngine.createEngine(CONFIG).state();

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
	void historyCellsAreIndependentCopies() {
		var bootstrap = SimulationEngine.createEngine(CONFIG);
		var engine = bootstrap.state();
		Agent original = engine.getHistory().get(0).cells().get(0);
		double originalX = original.getX();

		SimulationEngine.tickEngine(engine, CONFIG, bootstrap.rng());

		assertThat(engine.getHistory().get(0).cells().get(0).getX()).isEqualTo(originalX);
		assertThat(engine.getHistory().get(0).cells()).isNotSameAs(engine.getAgents());
		assertThat(engine.getHistory().get(1).cells()).isNotSameAs(engine.getAgents());
	}

	@Test
	void tickEngineContinuesRngFromCreateEngine() {
		var first = SimulationEngine.createEngine(CONFIG);
		var second = SimulationEngine.createEngine(CONFIG);

		SimulationEngine.tickEngine(first.state(), CONFIG, first.rng());
		SimulationEngine.tickEngine(second.state(), CONFIG, second.rng());

		assertThat(first.state().getAgents()).isEqualTo(second.state().getAgents());
		assertThat(first.state().getSnapshot()).isEqualTo(second.state().getSnapshot());
		assertThat(first.state().getHistory()).isEqualTo(second.state().getHistory());
		assertThat(first.state().getEvents()).isEqualTo(second.state().getEvents());
		assertThat(first.state().getStats()).isEqualTo(second.state().getStats());
		assertThat(first.state().getLinks()).isEqualTo(second.state().getLinks());
		assertThat(first.state().getSnapshot().day()).isEqualTo(1);
	}

	@Test
	void rejectsZeroPopulation() {
		var invalid = new SimulationConfig(0, 1.0, 0.25, 3, 7, 0.14, 42, "Hybrid (Agent + SEIR)", 100);

		assertThatThrownBy(() -> SimulationEngine.createEngine(invalid))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("populationSize");
	}

	@Test
	void statsUseTransmissionRateTimesInfectiousPeriodForR0() {
		var engine = SimulationEngine.createEngine(CONFIG).state();

		assertThat(engine.getStats().r0()).isEqualTo(1.75);
	}

	@Test
	void seededRandomMatchesTypescriptLcg() {
		var rng = new SeededRandom(42);

		assertThat(rng.nextDouble()).isEqualTo(705893.0 / 2147483646.0);
	}

	@Test
	void seededRandomAdvancesWhenSeedIsZero() {
		var rng = new SeededRandom(0);
		double first = rng.nextDouble();
		double second = rng.nextDouble();

		assertThat(first).isNotEqualTo(second);
		assertThat(first).isGreaterThanOrEqualTo(0.0);
		assertThat(first).isLessThan(1.0);
	}
}
