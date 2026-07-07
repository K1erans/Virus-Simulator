export type AgentState = 'S' | 'E' | 'I' | 'R';

export interface Agent {
	id: number;
	x: number;
	y: number;
	state: AgentState;
	exposedDays: number;
	infectiousDays: number;
}

export interface TransmissionLink {
	from: number;
	to: number;
}
