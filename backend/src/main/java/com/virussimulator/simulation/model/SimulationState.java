package com.virussimulator.simulation.model;

import java.util.List;

public class SimulationState {

	private List<Agent> agents;
	private SimulationSnapshot snapshot;
	private List<SimulationSnapshot> history;
	private List<SimulationEvent> events;
	private SimulationStats stats;
	private List<TransmissionLink> links;
	private boolean firstTransmissionLogged;

	public SimulationState(
			List<Agent> agents,
			SimulationSnapshot snapshot,
			List<SimulationSnapshot> history,
			List<SimulationEvent> events,
			SimulationStats stats,
			List<TransmissionLink> links,
			boolean firstTransmissionLogged) {
		this.agents = agents;
		this.snapshot = snapshot;
		this.history = history;
		this.events = events;
		this.stats = stats;
		this.links = links;
		this.firstTransmissionLogged = firstTransmissionLogged;
	}

	public List<Agent> getAgents() {
		return agents;
	}

	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}

	public SimulationSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(SimulationSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	public List<SimulationSnapshot> getHistory() {
		return history;
	}

	public void setHistory(List<SimulationSnapshot> history) {
		this.history = history;
	}

	public List<SimulationEvent> getEvents() {
		return events;
	}

	public void setEvents(List<SimulationEvent> events) {
		this.events = events;
	}

	public SimulationStats getStats() {
		return stats;
	}

	public void setStats(SimulationStats stats) {
		this.stats = stats;
	}

	public List<TransmissionLink> getLinks() {
		return links;
	}

	public void setLinks(List<TransmissionLink> links) {
		this.links = links;
	}

	public boolean isFirstTransmissionLogged() {
		return firstTransmissionLogged;
	}

	public void setFirstTransmissionLogged(boolean firstTransmissionLogged) {
		this.firstTransmissionLogged = firstTransmissionLogged;
	}
}
