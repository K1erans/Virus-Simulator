package com.virussimulator.websocket;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.virussimulator.simulation.dto.SnapshotMessage;
import com.virussimulator.simulation.service.SimulationService;

@Component
public class SimulationWebSocketHandler extends TextWebSocketHandler {

	private final SimulationSessionRegistry sessionRegistry;
	private final SimulationService simulationService;

	public SimulationWebSocketHandler(
			SimulationSessionRegistry sessionRegistry,
			SimulationService simulationService) {
		this.sessionRegistry = sessionRegistry;
		this.simulationService = simulationService;
	}

	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
		sessionRegistry.register(session);
		sessionRegistry.send(session, SnapshotMessage.connected("Simulation WebSocket connected"));
		sessionRegistry.send(session, simulationService.liveSyncMessage());
	}


	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		sessionRegistry.unregister(session);
	}
}
