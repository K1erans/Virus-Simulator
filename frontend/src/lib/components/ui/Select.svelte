<script lang="ts">
	interface Props {
		label: string;
		value?: string;
		options: string[];
	}

	let { label, value = $bindable(''), options }: Props = $props();
</script>

<label class="select">
	<span class="label">{label}</span>
	<select bind:value>
		{#each options as option}
			<option value={option}>{option}</option>
		{/each}
	</select>
</label>

<style>
	.select {
		display: block;
		margin-bottom: 0.875rem;
	}

	.label {
		display: block;
		margin-bottom: 0.375rem;
		font-size: 0.8125rem;
		color: var(--text-muted);
	}

	select {
		width: 100%;
		padding: 0.5rem 0.625rem;
		font-size: 0.8125rem;
		font-family: inherit;
		color: var(--text);
		background: var(--bg-page);
		border: 1px solid var(--border);
		border-radius: var(--radius-sm);
		outline: none;
		cursor: pointer;
	}

	select:focus {
		border-color: var(--susceptible);
	}
</style>
