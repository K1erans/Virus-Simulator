import { describe, expect, it } from 'vitest';

import { computeStats } from '$lib/simulation/dayStep';
import { createEngine, makeRng, tickEngine } from '$lib/simulation/mockEngine';
import type { SimulationConfig } from '$lib/types/simulation';

const config: SimulationConfig = {
	populationSize: 100,
	initialInfectedPct: 2,
	transmissionRate: 0.25,
	incubationPeriod: 3,
	infectiousPeriod: 7,
	recoveryRate: 0.14,
	randomSeed: 42,
	modelType: 'Hybrid (Agent + SEIR)',
	maxDays: 100
};

describe('simulation day step', () => {
	it('creates deterministic initial agents from the configured seed', () => {
		const first = createEngine(config).state;
		const second = createEngine(config).state;

		expect(first.agents.slice(0, 5)).toEqual(second.agents.slice(0, 5));
		expect(first.snapshot).toMatchObject({ day: 0, S: 98, E: 1, I: 1, R: 0, deaths: 0 });
	});

	it('continues the PRNG stream from createEngine into ticks', () => {
		const { state, rng } = createEngine(config);
		const before = state.agents.map((agent) => ({ ...agent }));

		tickEngine(state, config, rng, { firstTransmissionLogged: false });

		expect(state.snapshot.day).toBe(1);
		expect(state.history[0].cells).toEqual(before);
		expect(state.history[0].cells).not.toBe(state.agents);
		expect(state.history[1].cells).not.toBe(state.agents);
	});

	it('ticks deterministically with the same continued RNG stream', () => {
		const first = createEngine(config);
		const second = createEngine(config);

		tickEngine(first.state, config, first.rng, { firstTransmissionLogged: false });
		tickEngine(second.state, config, second.rng, { firstTransmissionLogged: false });

		expect(first.state.snapshot).toEqual(second.state.snapshot);
		expect(first.state.stats).toEqual(second.state.stats);
		expect(first.state.links).toEqual(second.state.links);
	});

	it('normalizes seed zero so the LCG advances', () => {
		const rng = makeRng(0);
		const first = rng();
		const second = rng();

		expect(first).not.toBe(second);
		expect(first).toBeGreaterThanOrEqual(0);
		expect(first).toBeLessThan(1);
	});

	it('computes r0 from transmission rate and infectious period', () => {
		const stats = computeStats({ S: 90, E: 3, I: 4, R: 3 }, config, 2, 4);

		expect(stats.r0).toBe(1.75);
	});
});
