import {
	connectSimulationSocket,
	pauseSimulation,
	resetSimulation,
	startSimulation
} from '$lib/api/client';
import type { SimulationEngine } from '$lib/simulation/engine';
import { createEngine } from '$lib/simulation/mockEngine';
import type { BatchMessage, CompleteMessage, DayDelta, LiveMessage } from '$lib/types/protocol';
import type { HistoryPoint, SimulationConfig, SimulationState, SnapshotMessage } from '$lib/types/simulation';

type RemoteStatus = {
	connected: boolean;
	error?: string | null;
};

type StatusListener = (status: RemoteStatus) => void;

function countsHistory(days: DayDelta[] | null | undefined, fallback: HistoryPoint[]): HistoryPoint[] {
	if (!days || days.length === 0) return fallback;
	return days.map((day) => ({
		day: day.day,
		S: day.S,
		E: day.E,
		I: day.I,
		R: day.R,
		deaths: day.deaths
	}));
}

function appendDays(existing: HistoryPoint[], days: DayDelta[] | null | undefined): HistoryPoint[] {
	if (!days || days.length === 0) return existing;

	const byDay = new Map(existing.map((point) => [point.day, point]));
	for (const day of days) {
		byDay.set(day.day, {
			day: day.day,
			S: day.S,
			E: day.E,
			I: day.I,
			R: day.R,
			deaths: day.deaths
		});
	}

	return [...byDay.values()].sort((a, b) => a.day - b.day);
}

export function createRemoteEngine(
	initialConfig: SimulationConfig,
	onStatus?: StatusListener
): SimulationEngine {
	let state = createEngine(initialConfig).state;
	let socket: WebSocket | null = null;
	const listeners = new Set<(state: SimulationState) => void>();

	function notify() {
		for (const listener of listeners) {
			listener(state);
		}
	}

	function setStatus(status: Partial<RemoteStatus>) {
		const next: RemoteStatus = {
			connected: status.connected ?? socket?.readyState === WebSocket.OPEN
		};
		if ('error' in status) {
			next.error = status.error ?? null;
		}
		onStatus?.(next);
	}

	function applyLiveOrComplete(message: LiveMessage | CompleteMessage) {
		state = {
			agents: message.agents,
			snapshot: message.snapshot,
			history: countsHistory(message.days, state.history),
			events: message.events,
			stats: message.stats,
			links: message.links
		};
		notify();
	}

function applyBatch(message: BatchMessage) {
	const mergedEvents = [...state.events];
	const seen = new Set(mergedEvents.map((event) => `${event.day}:${event.message}`));
	for (const event of message.events) {
		const key = `${event.day}:${event.message}`;
		if (!seen.has(key)) {
			seen.add(key);
			mergedEvents.push(event);
		}
	}

	state = {
		agents: message.agents,
		snapshot: message.snapshot,
		history: appendDays(state.history, message.days),
		events: mergedEvents,
		stats: message.stats,
		links: message.links
	};
	notify();
}

	function closeSocket() {
		if (socket) {
			socket.close();
			socket = null;
			setStatus({ connected: false });
		}
	}

	function ensureSocket() {
		if (socket && socket.readyState !== WebSocket.CLOSED) return;

		socket = connectSimulationSocket(
			(message) => {
				switch (message.type) {
					case 'connected':
						setStatus({ connected: true });
						return;
					case 'live':
					case 'complete':
						applyLiveOrComplete(message);
						setStatus({ connected: true });
						return;
					case 'batch':
						applyBatch(message);
						setStatus({ connected: true });
						return;
					case 'error':
						setStatus({ connected: false, error: message.message });
				}
			},
			() => setStatus({ connected: false, error: 'Simulation WebSocket error' })
		);

		socket.addEventListener('open', () => setStatus({ connected: true }));
		socket.addEventListener('close', () => setStatus({ connected: false }));
	}

	return {
		async start(config) {
			ensureSocket();
			await startSimulation(config);
		},

		async pause() {
			await pauseSimulation();
		},

		async reset(config) {
			state = createEngine(config).state;
			notify();
			ensureSocket();
			await resetSimulation(config);
		},

		destroy() {
			closeSocket();
			listeners.clear();
		},

		subscribe(listener) {
			listeners.add(listener);
			listener(state);
			return () => listeners.delete(listener);
		}
	};
}
