<script lang="ts">
	import Checkbox from '$lib/components/ui/Checkbox.svelte';
	import Card from '$lib/components/ui/Card.svelte';
	import { simulation } from '$lib/stores/simulation.svelte';
</script>

<Card title="View Options">
	<Checkbox label="Agent View" bind:checked={simulation.viewOptions.agentView} />
	<Checkbox label="SEIR Curves" bind:checked={simulation.viewOptions.seirCurves} />
	<Checkbox label="Population Stats" bind:checked={simulation.viewOptions.populationStats} />
	<Checkbox label="Transmission Network" bind:checked={simulation.viewOptions.transmissionNetwork} />
</Card>
