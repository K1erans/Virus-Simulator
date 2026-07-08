<script lang="ts">
	interface Props {
		label: string;
		checked?: boolean;
	}

	let { label, checked = $bindable(false) }: Props = $props();
</script>

<label class="checkbox">
	<input type="checkbox" bind:checked />
	<span>{label}</span>
</label>

<style>
	.checkbox {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		padding: 0.375rem 0;
		font-size: 0.8125rem;
		color: var(--text-muted);
		cursor: pointer;
	}

	.checkbox:hover {
		color: var(--text);
	}

	input[type='checkbox'] {
		width: 14px;
		height: 14px;
		accent-color: var(--susceptible);
		cursor: pointer;
	}
</style>
