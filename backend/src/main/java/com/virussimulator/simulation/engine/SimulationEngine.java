package com.virussimulator.simulation.engine;

import java.util.ArrayList;
import java.util.List;

import com.virussimulator.simulation.model.Agent;
import com.virussimulator.simulation.model.AgentState;
import com.virussimulator.simulation.model.SimulationConfig;
import com.virussimulator.simulation.model.SimulationEvent;
import com.virussimulator.simulation.model.SimulationSnapshot;
import com.virussimulator.simulation.model.SimulationState;
import com.virussimulator.simulation.model.SimulationStats;
import com.virussimulator.simulation.model.TransmissionLink;

public final class SimulationEngine {

	public static final double TRANSMISSION_RADIUS = 0.12;

	private SimulationEngine() {
	}

	public static EngineBootstrap createEngine(SimulationConfig config) {
		if (config.populationSize() < 1) {
			throw new IllegalArgumentException("populationSize must be at least 1");
		}

		SeededRandom rng = new SeededRandom(config.randomSeed());
		List<Agent> agents = new ArrayList<>(config.populationSize());

		for (int i = 0; i < config.populationSize(); i++) {
			agents.add(new Agent(i, rng.nextDouble(), rng.nextDouble(), AgentState.S, 0, 0));
		}

		int initialInfected = Math.max(
				1,
				(int) Math.round(config.populationSize() * (config.initialInfectedPct() / 100.0)));
		initialInfected = Math.min(initialInfected, agents.size());
		for (int i = 0; i < initialInfected; i++) {
			setInfectionState(
					agents.get(i),
					i % 3 == 0 ? AgentState.I : AgentState.E,
					config,
					rng,
					true);
		}

		Counts counts = countStates(agents);
		SimulationSnapshot snapshot = new SimulationSnapshot(
				0,
				counts.susceptible(),
				counts.exposed(),
				counts.infectious(),
				counts.recovered(),
				0,
				copyAgents(agents));
		List<SimulationSnapshot> history = new ArrayList<>();
		history.add(snapshot);

		SimulationState state = new SimulationState(
				agents,
				snapshot,
				history,
				new ArrayList<>(List.of(new SimulationEvent(0, "Simulation initialized"))),
				computeStats(snapshot, config, 0, 0, 0),
				new ArrayList<>(),
				false);

		return new EngineBootstrap(state, rng);
	}

	public static List<SimulationEvent> tickEngine(
			SimulationState engine,
			SimulationConfig config,
			SeededRandom rng) {
		int day = engine.getSnapshot().day() + 1;
		List<SimulationEvent> newEvents = new ArrayList<>();

		SpreadResult spreadResult = spreadInfections(engine, config, rng, day, newEvents);

		for (Agent agent : engine.getAgents()) {
			if (agent.getState() == AgentState.E) {
				agent.setExposedDays(agent.getExposedDays() + 1);
				if (agent.getExposedDays() >= config.incubationPeriod()) {
					setInfectionState(agent, AgentState.I, config, rng, false);
				}
			} else if (agent.getState() == AgentState.I) {
				agent.setInfectiousDays(agent.getInfectiousDays() + 1);
				if (agent.getInfectiousDays() >= config.infectiousPeriod()
						|| rng.nextDouble() < config.recoveryRate()) {
					agent.setState(AgentState.R);
				}
			}
		}

		for (Agent agent : engine.getAgents()) {
			if (agent.getState() == AgentState.S || agent.getState() == AgentState.R) {
				agent.setX(clamp(agent.getX() + (rng.nextDouble() - 0.5) * 0.02, 0.02, 0.98));
				agent.setY(clamp(agent.getY() + (rng.nextDouble() - 0.5) * 0.02, 0.02, 0.98));
			}
		}

		Counts counts = countStates(engine.getAgents());
		int previousPeak = engine.getStats().peakInfectious();
		int previousPeakDay = engine.getStats().peakInfectiousDay();
		SimulationSnapshot snapshot = new SimulationSnapshot(
				day,
				counts.susceptible(),
				counts.exposed(),
				counts.infectious(),
				counts.recovered(),
				engine.getSnapshot().deaths(),
				copyAgents(engine.getAgents()));

		engine.setSnapshot(snapshot);
		engine.getHistory().add(snapshot);
		engine.setLinks(spreadResult.links().stream().limit(40).toList());
		engine.setStats(computeStats(snapshot, config, spreadResult.newInfections(), previousPeak, previousPeakDay));

		if (spreadResult.newInfections() > 10 && day > 5) {
			newEvents.add(new SimulationEvent(day, "Rapid increase in infections"));
		}
		if (snapshot.I() == 0
				&& snapshot.E() == 0
				&& day > 10
				&& counts.recovered() > config.populationSize() * 0.5) {
			newEvents.add(new SimulationEvent(day, "Outbreak subsiding \u2014 most agents recovered"));
		}

		engine.getEvents().addAll(newEvents);
		return newEvents;
	}

	public static List<Agent> copyAgents(List<Agent> agents) {
		List<Agent> copies = new ArrayList<>(agents.size());
		for (Agent agent : agents) {
			copies.add(agent.copy());
		}
		return copies;
	}

	public static List<SimulationSnapshot> copyHistory(List<SimulationSnapshot> history) {
		List<SimulationSnapshot> copies = new ArrayList<>(history.size());
		for (SimulationSnapshot snapshot : history) {
			copies.add(new SimulationSnapshot(
					snapshot.day(),
					snapshot.S(),
					snapshot.E(),
					snapshot.I(),
					snapshot.R(),
					snapshot.deaths(),
					snapshot.cells() == null ? null : copyAgents(snapshot.cells())));
		}
		return copies;
	}

	private static void setInfectionState(
			Agent agent,
			AgentState state,
			SimulationConfig config,
			SeededRandom rng,
			boolean partial) {
		agent.setState(state);
		if (state == AgentState.E) {
			agent.setExposedDays(partial ? (int) Math.floor(rng.nextDouble() * config.incubationPeriod()) : 0);
			agent.setInfectiousDays(0);
		} else {
			agent.setInfectiousDays(partial ? (int) Math.floor(rng.nextDouble() * config.infectiousPeriod()) : 0);
			agent.setExposedDays(0);
		}
	}

	private static SpreadResult spreadInfections(
			SimulationState engine,
			SimulationConfig config,
			SeededRandom rng,
			int day,
			List<SimulationEvent> newEvents) {
		int newInfections = 0;
		List<TransmissionLink> links = new ArrayList<>();
		List<Agent> agents = engine.getAgents();

		for (int i = 0; i < agents.size(); i++) {
			Agent source = agents.get(i);
			if (source.getState() != AgentState.I) {
				continue;
			}

			for (int j = 0; j < agents.size(); j++) {
				if (i == j) {
					continue;
				}

				Agent target = agents.get(j);
				if (target.getState() != AgentState.S) {
					continue;
				}

				double dist = distance(source, target);
				if (dist > TRANSMISSION_RADIUS) {
					continue;
				}

				double probability = config.transmissionRate() * (1 - dist / TRANSMISSION_RADIUS);
				if (rng.nextDouble() >= probability) {
					continue;
				}

				setInfectionState(target, AgentState.E, config, rng, false);
				newInfections++;
				links.add(new TransmissionLink(i, j));

				if (!engine.isFirstTransmissionLogged()) {
					engine.setFirstTransmissionLogged(true);
					newEvents.add(new SimulationEvent(day, "First transmission occurred"));
				}
			}
		}

		return new SpreadResult(newInfections, links);
	}

	private static Counts countStates(List<Agent> agents) {
		int susceptible = 0;
		int exposed = 0;
		int infectious = 0;
		int recovered = 0;

		for (Agent agent : agents) {
			if (agent.getState() == AgentState.S) {
				susceptible++;
			} else if (agent.getState() == AgentState.E) {
				exposed++;
			} else if (agent.getState() == AgentState.I) {
				infectious++;
			} else {
				recovered++;
			}
		}

		return new Counts(susceptible, exposed, infectious, recovered);
	}

	private static SimulationStats computeStats(
			SimulationSnapshot snapshot,
			SimulationConfig config,
			int newInfections,
			int previousPeak,
			int previousPeakDay) {
		int totalInfected = snapshot.E() + snapshot.I() + snapshot.R();
		double attackRate = config.populationSize() == 0
				? 0.0
				: ((double) totalInfected / config.populationSize()) * 100.0;
		int activeCases = snapshot.E() + snapshot.I();
		double recoveryRatePct = totalInfected > 0 ? ((double) snapshot.R() / totalInfected) * 100.0 : 0.0;
		int peakInfectious = Math.max(previousPeak, snapshot.I());
		int peakInfectiousDay = snapshot.I() > previousPeak ? snapshot.day() : previousPeakDay;

		return new SimulationStats(
				peakInfectious,
				peakInfectiousDay,
				totalInfected,
				attackRate,
				config.transmissionRate() * config.infectiousPeriod(),
				newInfections,
				activeCases,
				recoveryRatePct);
	}

	private static double distance(Agent a, Agent b) {
		double dx = a.getX() - b.getX();
		double dy = a.getY() - b.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private record Counts(int susceptible, int exposed, int infectious, int recovered) {
	}

	private record SpreadResult(int newInfections, List<TransmissionLink> links) {
	}
}
