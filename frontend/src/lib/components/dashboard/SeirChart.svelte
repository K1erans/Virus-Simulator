<script lang="ts">
	import Card from '$lib/components/ui/Card.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';

	const width = 480;
	const height = 280;
	const pad = { top: 16, right: 80, bottom: 32, left: 48 };
	const plotW = width - pad.left - pad.right;
	const plotH = height - pad.top - pad.bottom;

	const maxDay = $derived(simulation.config.maxDays);
	const maxPop = $derived(simulation.config.populationSize);

	function xScale(day: number) {
		return pad.left + (day / maxDay) * plotW;
	}

	function yScale(value: number) {
		return pad.top + plotH - (value / maxPop) * plotH;
	}

	function linePath(key: 'S' | 'E' | 'I' | 'R') {
		const history = simulation.history;
		if (history.length === 0) return '';
		return history
			.map((pt, i) => `${i === 0 ? 'M' : 'L'} ${xScale(pt.day)} ${yScale(pt[key])}`)
			.join(' ');
	}

	const yTicks = $derived([0, maxPop * 0.25, maxPop * 0.5, maxPop * 0.75, maxPop]);
	const xTicks = $derived([0, maxDay * 0.25, maxDay * 0.5, maxDay * 0.75, maxDay].map(Math.round));

	const series = [
		{ key: 'S' as const, label: 'Susceptible', color: 'var(--susceptible)' },
		{ key: 'E' as const, label: 'Exposed', color: 'var(--exposed)' },
		{ key: 'I' as const, label: 'Infectious', color: 'var(--infectious)' },
		{ key: 'R' as const, label: 'Recovered', color: 'var(--recovered)' }
	];
</script>

<Card title="SEIR Curves">
	<div class="chart-wrap">
		<svg viewBox="0 0 {width} {height}" preserveAspectRatio="xMidYMid meet" aria-label="SEIR curves chart">
			<!-- grid -->
			{#each yTicks as tick}
				<line
					x1={pad.left}
					y1={yScale(tick)}
					x2={width - pad.right}
					y2={yScale(tick)}
					class="grid"
				/>
				<text x={pad.left - 8} y={yScale(tick) + 4} class="axis-label" text-anchor="end">
					{Math.round(tick)}
				</text>
			{/each}

			{#each xTicks as tick}
				<text x={xScale(tick)} y={height - 8} class="axis-label" text-anchor="middle">{tick}</text>
			{/each}

			<text x={width / 2} y={height - 0} class="axis-title" text-anchor="middle">Time (days)</text>
			<text
				x={12}
				y={height / 2}
				class="axis-title"
				text-anchor="middle"
				transform="rotate(-90 12 {height / 2})"
			>
				Population
			</text>

			{#each series as s}
				<path d={linePath(s.key)} fill="none" stroke={s.color} stroke-width="2" />
			{/each}

			<!-- legend -->
			{#each series as s, i}
				<line
					x1={width - pad.right + 8}
					y1={pad.top + i * 18}
					x2={width - pad.right + 24}
					y2={pad.top + i * 18}
					stroke={s.color}
					stroke-width="2"
				/>
				<text x={width - pad.right + 28} y={pad.top + i * 18 + 4} class="legend-label">{s.label}</text>
			{/each}
		</svg>
	</div>
</Card>

<style>
	.chart-wrap {
		width: 100%;
	}

	svg {
		width: 100%;
		height: auto;
		display: block;
	}

	.grid {
		stroke: var(--border);
		stroke-width: 0.5;
	}

	.axis-label {
		fill: var(--text-dim);
		font-size: 10px;
		font-family: system-ui, sans-serif;
	}

	.axis-title {
		fill: var(--text-muted);
		font-size: 10px;
		font-family: system-ui, sans-serif;
	}

	.legend-label {
		fill: var(--text-muted);
		font-size: 10px;
		font-family: system-ui, sans-serif;
	}
</style>
