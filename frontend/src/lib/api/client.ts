import type {
	HistoryPoint,
	SimulationConfig,
	SimulationStatusResponse,
	SnapshotMessage
} from '$lib/types/simulation';

export type HealthResponse = {
	status: string;
	service: string;
};

export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false';

export async function fetchHealth(): Promise<HealthResponse> {
	const response = await fetch('/api/health');

	if (!response.ok) {
		throw new Error(`Health check failed: ${response.status}`);
	}

	return response.json();
}

export async function startSimulation(config: SimulationConfig): Promise<SimulationStatusResponse> {
	const response = await fetch('/api/simulation/start', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(config)
	});

	if (!response.ok) {
		throw new Error(`Start simulation failed: ${response.status}`);
	}

	return response.json();
}

export async function pauseSimulation(): Promise<SimulationStatusResponse> {
	const response = await fetch('/api/simulation/pause', { method: 'POST' });

	if (!response.ok) {
		throw new Error(`Pause simulation failed: ${response.status}`);
	}

	return response.json();
}

export async function resetSimulation(config: SimulationConfig): Promise<SimulationStatusResponse> {
	const response = await fetch('/api/simulation/reset', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(config)
	});

	if (!response.ok) {
		throw new Error(`Reset simulation failed: ${response.status}`);
	}

	return response.json();
}

export async function fetchSimulationConfig(): Promise<SimulationConfig> {
	const response = await fetch('/api/simulation/config');

	if (!response.ok) {
		throw new Error(`Fetch config failed: ${response.status}`);
	}

	return response.json();
}

/** Full server history with agent cells — use when scrubbing / going back in time. */
export async function fetchSimulationHistory(): Promise<HistoryPoint[]> {
	const response = await fetch('/api/simulation/history');

	if (!response.ok) {
		throw new Error(`Fetch history failed: ${response.status}`);
	}

	return response.json();
}

export function connectSimulationSocket(
	onMessage: (data: SnapshotMessage) => void,
	onError?: (event: Event) => void
): WebSocket {
	const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
	const socket = new WebSocket(`${protocol}//${window.location.host}/ws/simulation`);

	socket.addEventListener('message', (event) => {
		try {
			onMessage(JSON.parse(event.data));
		} catch {
			onMessage({ type: 'error', message: String(event.data) });
		}
	});

	if (onError) {
		socket.addEventListener('error', onError);
	}

	return socket;
}
