<script lang="ts">
	import Slider from '$lib/components/ui/Slider.svelte';
	import Select from '$lib/components/ui/Select.svelte';
	import Input from '$lib/components/ui/Input.svelte';
	import Card from '$lib/components/ui/Card.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';

	const modelOptions = ['Hybrid (Agent + SEIR)', 'Agent Only', 'SEIR Only'];

	let advancedOpen = $state(false);
</script>

<Card title="Simulation Controls">
	<Slider
		label="Population Size"
		bind:value={simulation.config.populationSize}
		min={100}
		max={5000}
		step={100}
	/>
	<Slider
		label="Initial Infected (%)"
		bind:value={simulation.config.initialInfectedPct}
		min={0.1}
		max={10}
		step={0.1}
		format={(v) => `${v.toFixed(1)}%`}
	/>
	<Slider
		label="Transmission Rate (β)"
		bind:value={simulation.config.transmissionRate}
		min={0.05}
		max={1}
		step={0.01}
		format={(v) => v.toFixed(2)}
	/>
	<Slider
		label="Incubation Period (days)"
		bind:value={simulation.config.incubationPeriod}
		min={1}
		max={14}
		step={1}
	/>
	<Slider
		label="Infectious Period (days)"
		bind:value={simulation.config.infectiousPeriod}
		min={1}
		max={21}
		step={1}
	/>
	<Slider
		label="Recovery Rate (γ)"
		bind:value={simulation.config.recoveryRate}
		min={0.01}
		max={0.5}
		step={0.01}
		format={(v) => v.toFixed(2)}
	/>

	<Slider
		label="Simulation Duration (days)"
		bind:value={simulation.config.maxDays}
		min={10}
		max={1000}
		step={10}
	/>

	<Input label="Random Seed" type="number" bind:value={simulation.config.randomSeed} />
	<Select label="Model Type" bind:value={simulation.config.modelType} options={modelOptions} />

	<button type="button" class="advanced" onclick={() => (advancedOpen = !advancedOpen)}>
		Advanced Options
		<svg
			width="12"
			height="12"
			viewBox="0 0 24 24"
			fill="currentColor"
			class:open={advancedOpen}
			aria-hidden="true"
		>
			<path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z" />
		</svg>
	</button>

	{#if advancedOpen}
		<p class="advanced-note">Advanced parameters use the sliders above. Backend integration coming soon.</p>
	{/if}
</Card>

<style>
	.advanced {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		width: 100%;
		padding: 0.5rem 0;
		font-size: 0.8125rem;
		font-family: inherit;
		color: var(--text-muted);
		background: none;
		border: none;
		cursor: pointer;
	}

	.advanced:hover {
		color: var(--text);
	}

	svg {
		transition: transform 0.15s;
	}

	svg.open {
		transform: rotate(90deg);
	}

	.advanced-note {
		margin: 0.5rem 0 0;
		font-size: 0.75rem;
		color: var(--text-dim);
	}
</style>
