<script lang="ts">
	import Card from '$lib/components/ui/Card.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';

	let logEl = $state<HTMLDivElement | undefined>();

	$effect.pre(() => {
		simulation.events.length;
		if (!logEl) return;
		logEl.scrollTop = logEl.scrollHeight;
	});
</script>

<Card title="Event Log">
	<div class="log" bind:this={logEl}>
		{#each simulation.events as event (event.day + event.message)}
			<p><span class="day">Day {event.day}:</span> {event.message}</p>
		{/each}
	</div>
</Card>

<style>
	.log {
		max-height: 180px;
		overflow-y: auto;
		font-size: 0.8125rem;
		font-family: var(--font-mono);
	}

	.log p {
		margin: 0.375rem 0;
		color: var(--text-muted);
	}

	.day {
		color: var(--text-dim);
	}
</style>
