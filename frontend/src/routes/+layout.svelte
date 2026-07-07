<script lang="ts">
	let { children } = $props();
</script>

{@render children()}

<style>
	:global(body) {
		margin: 0;
		font-family: system-ui, sans-serif;
		background: #0f172a;
		color: #e2e8f0;
	}
</style>
