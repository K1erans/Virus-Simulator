<script lang="ts">
	interface Props {
		label: string;
		count: number;
		percent?: number;
		color?: string;
	}

	let { label, count, percent, color }: Props = $props();
</script>

<div class="stat-card">
	<span class="label">{label}</span>
	<span class="count" style:color={color}>{count.toLocaleString()}</span>
	{#if percent !== undefined}
		<span class="percent" style:color={color}>{percent.toFixed(1)}%</span>
	{/if}
</div>

<style>
	.stat-card {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
		padding: 0.875rem 1rem;
		background: var(--bg-panel);
		border: 1px solid var(--border);
		border-radius: var(--radius);
	}

	.label {
		font-size: 0.75rem;
		color: var(--text-muted);
	}

	.count {
		font-size: 1.375rem;
		font-weight: 600;
		font-variant-numeric: tabular-nums;
		line-height: 1.2;
	}

	.percent {
		font-size: 0.8125rem;
		font-variant-numeric: tabular-nums;
	}
</style>
