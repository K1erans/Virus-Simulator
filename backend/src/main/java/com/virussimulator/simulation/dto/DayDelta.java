package com.virussimulator.simulation.dto;

import com.virussimulator.simulation.model.SimulationSnapshot;

/** Counts-only day point for live streaming (no agent cells). */
public record DayDelta(int day, int S, int E, int I, int R, int deaths) {

	public static DayDelta from(SimulationSnapshot snapshot) {
		return new DayDelta(
				snapshot.day(),
				snapshot.S(),
				snapshot.E(),
				snapshot.I(),
				snapshot.R(),
				snapshot.deaths());
	}

	public SimulationSnapshot toCountsSnapshot() {
		return new SimulationSnapshot(day, S, E, I, R, deaths, null);
	}
}
