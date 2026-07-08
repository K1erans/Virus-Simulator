<script lang="ts">
	import StatCard from '$lib/components/ui/StatCard.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';

	const { snapshot, config } = $derived(simulation);
	const total = $derived(config.populationSize);
	const sPct = $derived((snapshot.S / total) * 100);
	const ePct = $derived((snapshot.E / total) * 100);
	const iPct = $derived((snapshot.I / total) * 100);
	const rPct = $derived((snapshot.R / total) * 100);
</script>

<div class="overview">
	<StatCard label="Total Population" count={total} color="var(--susceptible)" />
	<StatCard label="Susceptible (S)" count={snapshot.S} percent={sPct} color="var(--susceptible)" />
	<StatCard label="Exposed (E)" count={snapshot.E} percent={ePct} color="var(--exposed)" />
	<StatCard label="Infectious (I)" count={snapshot.I} percent={iPct} color="var(--infectious)" />
	<StatCard label="Recovered (R)" count={snapshot.R} percent={rPct} color="var(--recovered)" />
</div>

<style>
	.overview {
		display: grid;
		grid-template-columns: repeat(5, 1fr);
		gap: 0.75rem;
	}

	@media (max-width: 1200px) {
		.overview {
			grid-template-columns: repeat(3, 1fr);
		}
	}

	@media (max-width: 640px) {
		.overview {
			grid-template-columns: repeat(2, 1fr);
		}
	}
</style>
