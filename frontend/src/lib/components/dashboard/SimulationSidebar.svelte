<script lang="ts">
	import SimulationControls from './SimulationControls.svelte';
	import ViewOptions from './ViewOptions.svelte';
</script>

<aside class="sidebar">
	<SimulationControls />
	<ViewOptions />
</aside>

<style>
	.sidebar {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		padding: 1rem;
		border-right: 1px solid var(--border);
	}
</style>
