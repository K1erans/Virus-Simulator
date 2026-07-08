package com.virussimulator.simulation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.virussimulator.simulation.dto.DayDelta;
import com.virussimulator.simulation.dto.SimulationConfigPatch;
import com.virussimulator.simulation.dto.SimulationStatusResponse;
import com.virussimulator.simulation.dto.SnapshotMessage;
import com.virussimulator.simulation.engine.EngineBootstrap;
import com.virussimulator.simulation.engine.SeededRandom;
import com.virussimulator.simulation.engine.SimulationEngine;
import com.virussimulator.simulation.model.SimulationConfig;
import com.virussimulator.simulation.model.SimulationEvent;
import com.virussimulator.simulation.model.SimulationSnapshot;
import com.virussimulator.simulation.model.SimulationState;
import com.virussimulator.websocket.SimulationSessionRegistry;

import jakarta.annotation.PreDestroy;

@Service
public class SimulationService {

	public static final int TICK_MS = 200;
	public static final int BATCH_SIZE = 5;

	private final SimulationSessionRegistry sessionRegistry;
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "simulation-ticker");
		thread.setDaemon(true);
		return thread;
	});

	private SimulationConfig config = defaultConfig();
	private SimulationState state;
	private SeededRandom rng;
	private ScheduledFuture<?> tickTask;
	private final List<DayDelta> pendingDays = new ArrayList<>();
	private final List<SimulationEvent> pendingEvents = new ArrayList<>();

	public SimulationService(SimulationSessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
		resetStateLocked();
	}

	public synchronized SimulationStatusResponse start(SimulationConfigPatch patch) {
		if (isRunningLocked()) {
			return new SimulationStatusResponse("already_running", true);
		}

		applyPatchIfPresent(patch);

		if (isCompleteLocked()) {
			return finishAsCompleteLocked();
		}

		recordStartEventIfNeededLocked();
		scheduleTicksLocked();
		broadcastLiveLocked();
		return new SimulationStatusResponse("started", true);
	}

	public synchronized SimulationStatusResponse pause() {
		stopLocked();
		flushBatchLocked();
		return new SimulationStatusResponse("paused", false);
	}

	public synchronized SimulationStatusResponse reset(SimulationConfigPatch patch) {
		stopLocked();
		replaceConfigIfPresent(patch);
		resetStateLocked();
		broadcastLiveLocked();
		return new SimulationStatusResponse("reset", false);
	}

	public synchronized SimulationConfig getConfig() {
		return config;
	}

	public synchronized SimulationState getState() {
		return state;
	}

	public synchronized List<SimulationSnapshot> getHistory() {
		return SimulationEngine.copyHistory(state.getHistory());
	}

	public synchronized SnapshotMessage liveSyncMessage() {
		return SnapshotMessage.live(state, countsSeriesLocked());
	}

	public synchronized boolean isRunning() {
		return isRunningLocked();
	}

	@PreDestroy
	public synchronized void shutdown() {
		stopLocked();
		scheduler.shutdownNow();
	}

	public static SimulationConfig defaultConfig() {
		return new SimulationConfig(
				1000,
				1.0,
				0.25,
				3,
				7,
				0.14,
				42,
				"Hybrid (Agent + SEIR)",
				100);
	}

	private void runTick() {
		synchronized (this) {
			advanceOneDayLocked();
		}
	}

	private void advanceOneDayLocked() {
		if (!isRunningLocked()) {
			return;
		}

		List<SimulationEvent> tickEvents = SimulationEngine.tickEngine(state, config, rng);
		pendingDays.add(DayDelta.from(state.getSnapshot()));
		pendingEvents.addAll(tickEvents);

		if (isCompleteLocked()) {
			finishRunningAsCompleteLocked();
			return;
		}

		flushBatchIfFullLocked();
	}

	private void applyPatchIfPresent(SimulationConfigPatch patch) {
		if (patch == null) {
			return;
		}

		SimulationConfig merged = validatedMerge(patch);
		if (merged.equals(config)) {
			return;
		}

		config = merged;
		resetStateLocked();
	}

	private void replaceConfigIfPresent(SimulationConfigPatch patch) {
		if (patch == null) {
			return;
		}

		config = validatedMerge(patch);
	}

	private SimulationConfig validatedMerge(SimulationConfigPatch patch) {
		SimulationConfig merged = patch.mergeInto(config);
		validateConfig(merged);
		return merged;
	}

	private SimulationStatusResponse finishAsCompleteLocked() {
		addCompletionEventLocked();
		flushBatchLocked();
		broadcastCompleteLocked();
		return new SimulationStatusResponse("complete", false);
	}

	private void finishRunningAsCompleteLocked() {
		stopLocked();
		addCompletionEventLocked();
		flushBatchLocked();
		broadcastCompleteLocked();
	}

	private void recordStartEventIfNeededLocked() {
		if (state.getSnapshot().day() != 0) {
			return;
		}
		if (state.getEvents().size() > 1) {
			return;
		}

		state.getEvents().add(new SimulationEvent(0, "Simulation started"));
	}

	private void scheduleTicksLocked() {
		tickTask = scheduler.scheduleAtFixedRate(this::runTick, TICK_MS, TICK_MS, TimeUnit.MILLISECONDS);
	}

	private void broadcastLiveLocked() {
		sessionRegistry.broadcast(SnapshotMessage.live(state, countsSeriesLocked()));
	}

	private void broadcastCompleteLocked() {
		sessionRegistry.broadcast(SnapshotMessage.complete(state, completeMessage(), List.of()));
	}

	private void flushBatchIfFullLocked() {
		if (pendingDays.size() < BATCH_SIZE) {
			return;
		}

		flushBatchLocked();
	}

	private void flushBatchLocked() {
		if (pendingDays.isEmpty()) {
			return;
		}

		List<DayDelta> days = List.copyOf(pendingDays);
		List<SimulationEvent> events = List.copyOf(pendingEvents);
		pendingDays.clear();
		pendingEvents.clear();
		sessionRegistry.broadcast(SnapshotMessage.batch(state, days, events));
	}

	private List<DayDelta> countsSeriesLocked() {
		return state.getHistory().stream().map(DayDelta::from).toList();
	}

	private void resetStateLocked() {
		pendingDays.clear();
		pendingEvents.clear();
		EngineBootstrap bootstrap = SimulationEngine.createEngine(config);
		state = bootstrap.state();
		rng = bootstrap.rng();
	}

	private void stopLocked() {
		if (tickTask == null) {
			return;
		}

		tickTask.cancel(false);
		tickTask = null;
	}

	private boolean isRunningLocked() {
		return tickTask != null && !tickTask.isCancelled() && !tickTask.isDone();
	}

	private boolean isCompleteLocked() {
		return state.getSnapshot().day() >= config.maxDays();
	}

	private void addCompletionEventLocked() {
		String message = completeMessage();
		if (hasEventLocked(message)) {
			return;
		}

		state.getEvents().add(new SimulationEvent(state.getSnapshot().day(), message));
	}

	private boolean hasEventLocked(String message) {
		return state.getEvents().stream().anyMatch(event -> message.equals(event.message()));
	}

	private String completeMessage() {
		return "Simulation complete (" + config.maxDays() + " days)";
	}

	private static void validateConfig(SimulationConfig config) {
		if (config.populationSize() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "populationSize must be at least 1");
		}
		if (config.maxDays() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "maxDays must be at least 1");
		}
	}
}
