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
		const first = createEngine(config);
		const second = createEngine(config);

		expect(first.agents.slice(0, 5)).toEqual(second.agents.slice(0, 5));
		expect(first.snapshot).toMatchObject({ day: 0, S: 98, E: 1, I: 1, R: 0, deaths: 0 });
	});

	it('ticks deterministically with the same seed and runtime state', () => {
		const first = createEngine(config);
		const second = createEngine(config);

		tickEngine(first, config, makeRng(config.randomSeed), { firstTransmissionLogged: false });
		tickEngine(second, config, makeRng(config.randomSeed), { firstTransmissionLogged: false });

		expect(first.snapshot).toEqual(second.snapshot);
		expect(first.stats).toEqual(second.stats);
		expect(first.links).toEqual(second.links);
	});

	it('computes r0 from transmission rate and infectious period', () => {
		const stats = computeStats({ S: 90, E: 3, I: 4, R: 3 }, config, 2, 4);

		expect(stats.r0).toBe(1.75);
	});
});
