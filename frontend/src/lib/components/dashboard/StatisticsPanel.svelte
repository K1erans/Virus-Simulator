<script lang="ts">
	import Card from '$lib/components/ui/Card.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';

	const { snapshot, stats } = $derived(simulation);
</script>

<Card title="Statistics">
	<div class="stats">
		<div class="stat">
			<span class="label">Peak Infectious</span>
			<span class="value infectious">{stats.peakInfectious}</span>
			<span class="sub">at Day {stats.peakInfectiousDay}</span>
		</div>
		<div class="stat">
			<span class="label">Total Infected</span>
			<span class="value">{stats.totalInfected}</span>
			<span class="sub">{stats.attackRate.toFixed(1)}%</span>
		</div>
		<div class="stat">
			<span class="label">Attack Rate</span>
			<span class="value">{stats.attackRate.toFixed(1)}%</span>
		</div>
		<div class="stat">
			<span class="label">R₀ (Estimated)</span>
			<span class="value">{stats.r0.toFixed(2)}</span>
		</div>
		<div class="stat">
			<span class="label">Simulation Time</span>
			<span class="value">{snapshot.day} days</span>
		</div>
		<div class="stat">
			<span class="label">Time Step</span>
			<span class="value">1 day</span>
		</div>
	</div>
</Card>

<style>
	.stats {
		display: grid;
		grid-template-columns: repeat(6, 1fr);
		gap: 1rem;
	}

	.stat {
		display: flex;
		flex-direction: column;
		gap: 0.125rem;
	}

	.label {
		font-size: 0.75rem;
		color: var(--text-muted);
	}

	.value {
		font-size: 1.125rem;
		font-weight: 600;
		font-variant-numeric: tabular-nums;
	}

	.value.infectious {
		color: var(--infectious);
	}

	.sub {
		font-size: 0.75rem;
		color: var(--text-dim);
	}

	@media (max-width: 900px) {
		.stats {
			grid-template-columns: repeat(3, 1fr);
		}
	}
</style>
