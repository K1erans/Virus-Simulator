<script lang="ts">
	interface Props {
		label: string;
		value?: string | number;
		type?: 'text' | 'number';
	}

	let { label, value = $bindable(''), type = 'text' }: Props = $props();
</script>

<label class="input">
	<span class="label">{label}</span>
	<input {type} bind:value />
</label>

<style>
	.input {
		display: block;
		margin-bottom: 0.875rem;
	}

	.label {
		display: block;
		margin-bottom: 0.375rem;
		font-size: 0.8125rem;
		color: var(--text-muted);
	}

	input {
		width: 100%;
		padding: 0.5rem 0.625rem;
		font-size: 0.8125rem;
		font-family: inherit;
		color: var(--text);
		background: var(--bg-page);
		border: 1px solid var(--border);
		border-radius: var(--radius-sm);
		outline: none;
		box-sizing: border-box;
	}

	input:focus {
		border-color: var(--susceptible);
	}
</style>
