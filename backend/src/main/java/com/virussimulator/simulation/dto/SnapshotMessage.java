package com.virussimulator.simulation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.virussimulator.simulation.model.Agent;
import com.virussimulator.simulation.model.SimulationEvent;
import com.virussimulator.simulation.model.SimulationSnapshot;
import com.virussimulator.simulation.model.SimulationState;
import com.virussimulator.simulation.model.SimulationStats;
import com.virussimulator.simulation.model.TransmissionLink;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SnapshotMessage(
		String type,
		List<Agent> agents,
		SimulationSnapshot snapshot,
		List<SimulationSnapshot> history,
		List<SimulationEvent> events,
		SimulationStats stats,
		List<TransmissionLink> links,
		String message) {

	public static SnapshotMessage connected(String message) {
		return new SnapshotMessage("connected", null, null, null, null, null, null, message);
	}

	public static SnapshotMessage snapshot(SimulationState state) {
		return fromState("snapshot", state, null);
	}

	public static SnapshotMessage complete(SimulationState state, String message) {
		return fromState("complete", state, message);
	}

	private static SnapshotMessage fromState(String type, SimulationState state, String message) {
		return new SnapshotMessage(
				type,
				state.getAgents(),
				state.getSnapshot(),
				state.getHistory(),
				state.getEvents(),
				state.getStats(),
				state.getLinks(),
				message);
	}
}
