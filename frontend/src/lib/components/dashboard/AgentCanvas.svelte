<script lang="ts">
	import Card from '$lib/components/ui/Card.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';
	import type { AgentState } from '$lib/types/agent';

	let canvas = $state<HTMLCanvasElement | undefined>();
	let container = $state<HTMLDivElement | undefined>();

	const stateColors: Record<AgentState, string> = {
		S: '#3b82f6',
		E: '#eab308',
		I: '#ef4444',
		R: '#22c55e'
	};

	function draw() {
		if (!canvas || !container) return;

		const rect = container.getBoundingClientRect();
		const dpr = window.devicePixelRatio || 1;
		const w = rect.width;
		const h = Math.max(280, rect.width * 0.65);

		canvas.width = w * dpr;
		canvas.height = h * dpr;
		canvas.style.width = `${w}px`;
		canvas.style.height = `${h}px`;

		const ctx = canvas.getContext('2d');
		if (!ctx) return;

		ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
		ctx.fillStyle = '#0f172a';
		ctx.fillRect(0, 0, w, h);

		const showNetwork = simulation.viewOptions.transmissionNetwork;
		if (showNetwork && simulation.links.length > 0) {
			ctx.strokeStyle = 'rgba(148, 163, 184, 0.25)';
			ctx.lineWidth = 0.5;
			for (const link of simulation.links) {
				const from = simulation.agents[link.from];
				const to = simulation.agents[link.to];
				if (!from || !to) continue;
				ctx.beginPath();
				ctx.moveTo(from.x * w, from.y * h);
				ctx.lineTo(to.x * w, to.y * h);
				ctx.stroke();
			}
		}

		const radius = simulation.config.populationSize > 2000 ? 2 : 3;
		for (const agent of simulation.agents) {
			ctx.beginPath();
			ctx.fillStyle = stateColors[agent.state];
			ctx.arc(agent.x * w, agent.y * h, radius, 0, Math.PI * 2);
			ctx.fill();
		}
	}

	$effect(() => {
		simulation.agents;
		simulation.links;
		simulation.viewOptions.transmissionNetwork;
		draw();
	});

	$effect(() => {
		if (!container) return;

		const observer = new ResizeObserver(() => draw());
		observer.observe(container);
		return () => observer.disconnect();
	});
</script>

<Card title="Agent View">
	<div class="canvas-wrap" bind:this={container}>
		<canvas bind:this={canvas} aria-label="Agent scatter plot"></canvas>
	</div>
	<div class="legend">
		<span><i style:background="var(--susceptible)"></i> Susceptible</span>
		<span><i style:background="var(--exposed)"></i> Exposed</span>
		<span><i style:background="var(--infectious)"></i> Infectious</span>
		<span><i style:background="var(--recovered)"></i> Recovered</span>
	</div>
</Card>

<style>
	.canvas-wrap {
		width: 100%;
		min-height: 280px;
		border-radius: var(--radius-sm);
		overflow: hidden;
		background: var(--bg-page);
	}

	canvas {
		display: block;
		width: 100%;
	}

	.legend {
		display: flex;
		flex-wrap: wrap;
		gap: 1rem;
		margin-top: 0.75rem;
		font-size: 0.75rem;
		color: var(--text-muted);
	}

	.legend span {
		display: flex;
		align-items: center;
		gap: 0.375rem;
	}

	.legend i {
		display: inline-block;
		width: 8px;
		height: 8px;
		border-radius: 50%;
	}
</style>
