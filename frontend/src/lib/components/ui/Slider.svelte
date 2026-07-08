<script lang="ts">
	interface Props {
		label: string;
		value?: number;
		min?: number;
		max?: number;
		step?: number;
		format?: (v: number) => string;
	}

	let {
		label,
		value = $bindable(0),
		min = 0,
		max = 100,
		step = 1,
		format = (v) => String(v)
	}: Props = $props();
</script>

<label class="slider">
	<span class="label-row">
		<span class="label">{label}</span>
		<span class="value">{format(value)}</span>
	</span>
	<input type="range" bind:value {min} {max} {step} />
</label>

<style>
	.slider {
		display: block;
		margin-bottom: 0.875rem;
	}

	.label-row {
		display: flex;
		justify-content: space-between;
		align-items: baseline;
		margin-bottom: 0.375rem;
		font-size: 0.8125rem;
	}

	.label {
		color: var(--text-muted);
	}

	.value {
		color: var(--text);
		font-variant-numeric: tabular-nums;
	}

	input[type='range'] {
		width: 100%;
		height: 4px;
		appearance: none;
		background: var(--border);
		border-radius: 2px;
		outline: none;
	}

	input[type='range']::-webkit-slider-thumb {
		appearance: none;
		width: 14px;
		height: 14px;
		border-radius: 50%;
		background: var(--susceptible);
		cursor: pointer;
	}

	input[type='range']::-moz-range-thumb {
		width: 14px;
		height: 14px;
		border: none;
		border-radius: 50%;
		background: var(--susceptible);
		cursor: pointer;
	}
</style>
