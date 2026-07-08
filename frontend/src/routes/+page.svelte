<script lang="ts">
	import DashboardHeader from '$lib/components/dashboard/DashboardHeader.svelte';
	import SimulationSidebar from '$lib/components/dashboard/SimulationSidebar.svelte';
	import OverviewCards from '$lib/components/dashboard/OverviewCards.svelte';
	import AgentCanvas from '$lib/components/dashboard/AgentCanvas.svelte';
	import SeirChart from '$lib/components/dashboard/SeirChart.svelte';
	import StatisticsPanel from '$lib/components/dashboard/StatisticsPanel.svelte';
	import EventLog from '$lib/components/dashboard/EventLog.svelte';
	import RealtimeMetrics from '$lib/components/dashboard/RealtimeMetrics.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';
</script>

<div class="dashboard">
	<DashboardHeader />

	<div class="layout-body">
		<SimulationSidebar />

		<main class="content">
			{#if simulation.viewOptions.populationStats}
				<OverviewCards />
			{/if}

			<div class="charts-row">
				{#if simulation.viewOptions.agentView}
					<div class="chart-cell">
						<AgentCanvas />
					</div>
				{/if}
				{#if simulation.viewOptions.seirCurves}
					<div class="chart-cell">
						<SeirChart />
					</div>
				{/if}
			</div>

			{#if simulation.viewOptions.populationStats}
				<StatisticsPanel />
			{/if}

			<div class="bottom-row">
				<div class="bottom-cell">
					<EventLog />
				</div>
				<div class="bottom-cell">
					<RealtimeMetrics />
				</div>
			</div>
		</main>
	</div>
</div>

<style>
	.dashboard {
		display: flex;
		flex-direction: column;
		min-height: 100vh;
	}

	.layout-body {
		display: grid;
		grid-template-columns: var(--sidebar-width) 1fr;
		flex: 1;
	}

	.content {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		padding: 1rem 1.5rem;
		overflow-x: hidden;
	}

	.charts-row {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 1rem;
	}

	.chart-cell {
		min-width: 0;
	}

	.bottom-row {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 1rem;
	}

	.bottom-cell {
		min-width: 0;
	}

	@media (max-width: 1100px) {
		.layout-body {
			grid-template-columns: 1fr;
		}

		.charts-row {
			grid-template-columns: 1fr;
		}

		.bottom-row {
			grid-template-columns: 1fr;
		}
	}
</style>
