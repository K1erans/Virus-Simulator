<script lang="ts">
	import Button from '$lib/components/ui/Button.svelte';
	import { pause, reset, simulation, start } from '$lib/stores/simulation.svelte';

	const connectionLabel = $derived(
		simulation.source === 'local'
			? 'Local engine'
			: simulation.connected
				? 'Backend connected'
				: 'Backend disconnected'
	);
	const connectionClass = $derived(
		`connection ${simulation.source === 'backend' && !simulation.connected ? 'disconnected' : 'connected'}`
	);
</script>

<header class="header">
	<h1>Virus Simulator</h1>
	<div class="actions">
		<span class={connectionClass}>{connectionLabel}</span>
		<Button onclick={start} disabled={simulation.running || simulation.loading}>
			<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
				<path d="M8 5v14l11-7z" />
			</svg>
			Start
		</Button>
		<Button onclick={pause} disabled={!simulation.running || simulation.loading}>
			<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
				<path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z" />
			</svg>
			Pause
		</Button>
		<Button onclick={reset} disabled={simulation.loading}>
			<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
				<path
					d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"
				/>
			</svg>
			Reset
		</Button>
	</div>
</header>

<style>
	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 1rem 1.5rem;
		border-bottom: 1px solid var(--border);
	}

	h1 {
		margin: 0;
		font-size: 1.125rem;
		font-weight: 600;
	}

	.actions {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.connection {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		font-size: 0.75rem;
		color: var(--text-muted);
	}

	.connection::before {
		content: '';
		width: 0.5rem;
		height: 0.5rem;
		border-radius: 999px;
		background: var(--recovered);
	}

	.connection.disconnected::before {
		background: var(--text-dim);
	}
</style>
