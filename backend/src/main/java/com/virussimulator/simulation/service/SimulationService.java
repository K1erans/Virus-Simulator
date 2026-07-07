package com.virussimulator.simulation.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.virussimulator.simulation.dto.SimulationStatusResponse;
import com.virussimulator.simulation.dto.SnapshotMessage;
import com.virussimulator.simulation.engine.SeededRandom;
import com.virussimulator.simulation.engine.SimulationEngine;
import com.virussimulator.simulation.model.SimulationConfig;
import com.virussimulator.simulation.model.SimulationEvent;
import com.virussimulator.simulation.model.SimulationState;
import com.virussimulator.websocket.SimulationSessionRegistry;

import jakarta.annotation.PreDestroy;

@Service
public class SimulationService {

	public static final int TICK_MS = 200;

	private final SimulationSessionRegistry sessionRegistry;
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "simulation-ticker");
		thread.setDaemon(true);
		return thread;
	});

	private SimulationConfig config = defaultConfig();
	private SimulationState state = SimulationEngine.createEngine(config);
	private SeededRandom rng = makeRuntimeRng(config);
	private ScheduledFuture<?> tickTask;

	public SimulationService(SimulationSessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	public synchronized SimulationStatusResponse start(SimulationConfig requestedConfig) {
		if (isRunningLocked()) {
			return new SimulationStatusResponse("already_running", true);
		}

		if (requestedConfig != null && !requestedConfig.equals(config)) {
			config = requestedConfig;
			resetStateLocked();
		}

		if (state.getSnapshot().day() >= config.maxDays()) {
			addCompletionEventLocked();
			sessionRegistry.broadcast(SnapshotMessage.complete(state, completeMessage()));
			return new SimulationStatusResponse("complete", false);
		}

		if (state.getSnapshot().day() == 0 && state.getEvents().size() <= 1) {
			state.getEvents().add(new SimulationEvent(0, "Simulation started"));
		}

		tickTask = scheduler.scheduleAtFixedRate(this::runTick, TICK_MS, TICK_MS, TimeUnit.MILLISECONDS);
		sessionRegistry.broadcast(SnapshotMessage.snapshot(state));
		return new SimulationStatusResponse("started", true);
	}

	public synchronized SimulationStatusResponse pause() {
		stopLocked();
		return new SimulationStatusResponse("paused", false);
	}

	public synchronized SimulationStatusResponse reset() {
		stopLocked();
		resetStateLocked();
		sessionRegistry.broadcast(SnapshotMessage.snapshot(state));
		return new SimulationStatusResponse("reset", false);
	}

	public synchronized SimulationConfig getConfig() {
		return config;
	}

	public synchronized SimulationState getState() {
		return state;
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
			if (!isRunningLocked()) {
				return;
			}

			SimulationEngine.tickEngine(state, config, rng);
			if (state.getSnapshot().day() >= config.maxDays()) {
				stopLocked();
				addCompletionEventLocked();
				sessionRegistry.broadcast(SnapshotMessage.complete(state, completeMessage()));
				return;
			}

			sessionRegistry.broadcast(SnapshotMessage.snapshot(state));
		}
	}

	private void resetStateLocked() {
		state = SimulationEngine.createEngine(config);
		rng = makeRuntimeRng(config);
	}

	private void stopLocked() {
		if (tickTask != null) {
			tickTask.cancel(false);
			tickTask = null;
		}
	}

	private boolean isRunningLocked() {
		return tickTask != null && !tickTask.isCancelled() && !tickTask.isDone();
	}

	private void addCompletionEventLocked() {
		String message = completeMessage();
		boolean alreadyLogged = state.getEvents().stream()
				.anyMatch(event -> message.equals(event.message()));
		if (!alreadyLogged) {
			state.getEvents().add(new SimulationEvent(state.getSnapshot().day(), message));
		}
	}

	private String completeMessage() {
		return "Simulation complete (" + config.maxDays() + " days)";
	}

	private static SeededRandom makeRuntimeRng(SimulationConfig config) {
		return new SeededRandom(config.randomSeed());
	}
}
