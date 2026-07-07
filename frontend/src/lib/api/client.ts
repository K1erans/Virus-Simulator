export type HealthResponse = {
	status: string;
	service: string;
};

export async function fetchHealth(): Promise<HealthResponse> {
	const response = await fetch('/api/health');

	if (!response.ok) {
		throw new Error(`Health check failed: ${response.status}`);
	}

	return response.json();
}

export function connectSimulationSocket(
	onMessage: (data: unknown) => void,
	onError?: (event: Event) => void
): WebSocket {
	const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
	const socket = new WebSocket(`${protocol}//${window.location.host}/ws/simulation`);

	socket.addEventListener('message', (event) => {
		try {
			onMessage(JSON.parse(event.data));
		} catch {
			onMessage(event.data);
		}
	});

	if (onError) {
		socket.addEventListener('error', onError);
	}

	return socket;
}
