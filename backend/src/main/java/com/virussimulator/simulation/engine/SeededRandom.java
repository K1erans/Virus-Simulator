package com.virussimulator.simulation.engine;

public class SeededRandom {

	private static final long MULTIPLIER = 16807L;
	private static final long MODULUS = 2147483647L;
	private static final double DIVISOR = 2147483646.0;

	private long seed;

	public SeededRandom(long seed) {
		this.seed = seed;
	}

	public double nextDouble() {
		seed = (seed * MULTIPLIER) % MODULUS;
		return (seed - 1) / DIVISOR;
	}
}
