<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		variant?: 'default' | 'ghost';
		disabled?: boolean;
		onclick?: () => void;
		children: Snippet;
	}

	let { variant = 'default', disabled = false, onclick, children }: Props = $props();
</script>

<button type="button" class={variant} {disabled} {onclick}>
	{@render children()}
</button>

<style>
	button {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		padding: 0.5rem 0.875rem;
		font-size: 0.875rem;
		font-family: inherit;
		color: var(--text);
		background: var(--bg-panel);
		border: 1px solid var(--border);
		border-radius: var(--radius-sm);
		cursor: pointer;
		transition: background 0.15s, border-color 0.15s;
	}

	button:hover:not(:disabled) {
		background: var(--bg-panel-hover);
		border-color: var(--border-subtle);
	}

	button:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	button.ghost {
		background: transparent;
	}
</style>
