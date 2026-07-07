package com.virussimulator.websocket;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.virussimulator.simulation.dto.SnapshotMessage;

@Component
public class SimulationWebSocketHandler extends TextWebSocketHandler {

	private final SimulationSessionRegistry sessionRegistry;

	public SimulationWebSocketHandler(SimulationSessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
		sessionRegistry.register(session);
		sessionRegistry.send(session, SnapshotMessage.connected("Simulation WebSocket connected"));
	}

	@Override
	protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
		// TODO: handle client messages if needed
	}

	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		sessionRegistry.unregister(session);
	}
}
