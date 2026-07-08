package com.virussimulator.simulation.model;

import java.util.Objects;

public class Agent {

	private int id;
	private double x;
	private double y;
	private AgentState state;
	private int exposedDays;
	private int infectiousDays;

	public Agent(int id, double x, double y, AgentState state, int exposedDays, int infectiousDays) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.state = state;
		this.exposedDays = exposedDays;
		this.infectiousDays = infectiousDays;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public AgentState getState() {
		return state;
	}

	public void setState(AgentState state) {
		this.state = state;
	}

	public int getExposedDays() {
		return exposedDays;
	}

	public void setExposedDays(int exposedDays) {
		this.exposedDays = exposedDays;
	}

	public int getInfectiousDays() {
		return infectiousDays;
	}

	public void setInfectiousDays(int infectiousDays) {
		this.infectiousDays = infectiousDays;
	}

	public Agent copy() {
		return new Agent(id, x, y, state, exposedDays, infectiousDays);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Agent agent)) {
			return false;
		}
		return id == agent.id
				&& Double.compare(x, agent.x) == 0
				&& Double.compare(y, agent.y) == 0
				&& exposedDays == agent.exposedDays
				&& infectiousDays == agent.infectiousDays
				&& state == agent.state;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, x, y, state, exposedDays, infectiousDays);
	}
}
