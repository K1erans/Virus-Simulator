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
		List<DayDelta> days,
		List<SimulationEvent> events,
		SimulationStats stats,
		List<TransmissionLink> links,
		String message) {

	public static SnapshotMessage connected(String message) {
		return new SnapshotMessage("connected", null, null, null, null, null, null, null, message);
	}

	/** Live sync without full agent-cell history (counts series only). */
	public static SnapshotMessage live(SimulationState state, List<DayDelta> countsSeries) {
		return new SnapshotMessage(
				"live",
				state.getAgents(),
				countsOnly(state.getSnapshot()),
				null,
				countsSeries,
				state.getEvents(),
				state.getStats(),
				state.getLinks(),
				null);
	}

	/** Batched day advances: counts deltas + latest live fields. */
	public static SnapshotMessage batch(
			SimulationState state,
			List<DayDelta> days,
			List<SimulationEvent> newEvents) {
		return new SnapshotMessage(
				"batch",
				state.getAgents(),
				countsOnly(state.getSnapshot()),
				null,
				days,
				newEvents,
				state.getStats(),
				state.getLinks(),
				null);
	}

	public static SnapshotMessage complete(SimulationState state, String message, List<DayDelta> days) {
		return new SnapshotMessage(
				"complete",
				state.getAgents(),
				countsOnly(state.getSnapshot()),
				null,
				days,
				state.getEvents(),
				state.getStats(),
				state.getLinks(),
				message);
	}

	private static SimulationSnapshot countsOnly(SimulationSnapshot snapshot) {
		return new SimulationSnapshot(
				snapshot.day(),
				snapshot.S(),
				snapshot.E(),
				snapshot.I(),
				snapshot.R(),
				snapshot.deaths(),
				null);
	}
}
