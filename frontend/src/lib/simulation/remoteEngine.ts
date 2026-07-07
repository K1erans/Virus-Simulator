import {
	connectSimulationSocket,
	pauseSimulation,
	resetSimulation,
	startSimulation
} from '$lib/api/client';
import type { SimulationEngine } from '$lib/simulation/engine';
import { createEngine } from '$lib/simulation/mockEngine';
import type { SimulationConfig, SimulationState, SnapshotMessage } from '$lib/types/simulation';

type RemoteStatus = {
	connected: boolean;
	error: string | null;
};

type StatusListener = (status: RemoteStatus) => void;

export function createRemoteEngine(
	initialConfig: SimulationConfig,
	onStatus?: StatusListener
): SimulationEngine {
	let state = createEngine(initialConfig);
	let socket: WebSocket | null = null;
	const listeners = new Set<(state: SimulationState) => void>();

	function notify() {
		for (const listener of listeners) {
			listener(state);
		}
	}

	function setStatus(status: Partial<RemoteStatus>) {
		onStatus?.({
			connected: status.connected ?? socket?.readyState === WebSocket.OPEN,
			error: status.error ?? null
		});
	}

	function applySnapshot(message: Extract<SnapshotMessage, { type: 'snapshot' | 'complete' }>) {
		state = {
			agents: message.agents,
			snapshot: message.snapshot,
			history: message.history,
			events: message.events,
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
				if (message.type === 'connected') {
					setStatus({ connected: true, error: null });
				} else if (message.type === 'snapshot' || message.type === 'complete') {
					applySnapshot(message);
					setStatus({ connected: true, error: null });
				} else {
					setStatus({ connected: false, error: message.message });
				}
			},
			() => setStatus({ connected: false, error: 'Simulation WebSocket error' })
		);

		socket.addEventListener('open', () => setStatus({ connected: true, error: null }));
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
			state = createEngine(config);
			notify();
			ensureSocket();
			await resetSimulation();
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
