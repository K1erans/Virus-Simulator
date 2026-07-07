<script lang="ts">
	import Card from '$lib/components/ui/Card.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';

	const { snapshot, stats } = $derived(simulation);
</script>

<Card title="Real-Time Metrics">
	<div class="metrics">
		<div class="metric">
			<span class="label">Current Day</span>
			<span class="value">{snapshot.day}</span>
		</div>
		<div class="metric">
			<span class="label">New Infections</span>
			<span class="value infectious">+{stats.newInfections}</span>
		</div>
		<div class="metric">
			<span class="label">Active Cases</span>
			<span class="value infectious">{stats.activeCases}</span>
		</div>
		<div class="metric">
			<span class="label">Recovery Rate</span>
			<span class="value recovered">{stats.recoveryRatePct.toFixed(1)}%</span>
		</div>
	</div>
</Card>

<style>
	.metrics {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 0.75rem;
	}

	.metric {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
		padding: 0.75rem;
		background: var(--bg-page);
		border: 1px solid var(--border);
		border-radius: var(--radius-sm);
	}

	.label {
		font-size: 0.75rem;
		color: var(--text-muted);
	}

	.value {
		font-size: 1.25rem;
		font-weight: 600;
		font-variant-numeric: tabular-nums;
	}

	.value.infectious {
		color: var(--infectious);
	}

	.value.recovered {
		color: var(--recovered);
	}
</style>
