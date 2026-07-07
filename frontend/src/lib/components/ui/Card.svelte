<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		title?: string;
		class?: string;
		children: Snippet;
	}

	let { title, class: className = '', children }: Props = $props();
</script>

<section class="card {className}">
	{#if title}
		<h3 class="title">{title}</h3>
	{/if}
	{@render children()}
</section>

<style>
	.card {
		background: var(--bg-panel);
		border: 1px solid var(--border);
		border-radius: var(--radius);
		padding: 1rem;
	}

	.title {
		margin: 0 0 0.75rem;
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--text);
	}
</style>
