<style src="./+page.css"></style>

<script lang="ts">
	import { onMount } from 'svelte';
	import { connectSimulationSocket, fetchHealth } from '$lib/api/client';

	let apiStatus = $state<'checking' | 'connected' | 'error'>('checking');
	let apiMessage = $state('');
	let wsStatus = $state<'idle' | 'connecting' | 'connected' | 'error'>('idle');
	let wsMessage = $state('');

	onMount(() => {
		fetchHealth()
			.then((data) => {
				apiStatus = 'connected';
				apiMessage = `${data.service} — ${data.status}`;
			})
			.catch((error: Error) => {
				apiStatus = 'error';
				apiMessage = error.message;
			});

		wsStatus = 'connecting';
		const socket = connectSimulationSocket(
			(data) => {
				wsStatus = 'connected';
				if (typeof data === 'object' && data !== null && 'message' in data) {
					wsMessage = String((data as { message: string }).message);
				} else {
					wsMessage = JSON.stringify(data);
				}
			},
			() => {
				wsStatus = 'error';
				wsMessage = 'WebSocket connection failed';
			}
		);

		return () => socket.close();
	});
</script>

<main>
	<h1>Virus Simulator</h1>
	<p>Frontend and backend are linked. Implement the simulation from here.</p>

	<section>
		<h2>Connection status</h2>
		<ul>
			<li class={apiStatus}>REST API: {apiStatus} — {apiMessage || '…'}</li>
			<li class={wsStatus}>WebSocket: {wsStatus} — {wsMessage || '…'}</li>
		</ul>
	</section>

	<section>
		<h2>Where to implement</h2>
		<ul>
			<li><code>backend/src/main/java/com/virussimulator/simulation/</code> — simulation engine</li>
			<li><code>backend/.../controller/SimulationController.java</code> — start / pause / reset</li>
			<li><code>backend/.../websocket/SimulationWebSocketHandler.java</code> — tick streaming</li>
			<li><code>frontend/src/lib/components/</code> — canvas, chart, controls</li>
		</ul>
	</section>
</main>

