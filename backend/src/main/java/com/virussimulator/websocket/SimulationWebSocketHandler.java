package com.virussimulator.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SimulationWebSocketHandler extends TextWebSocketHandler {

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.sendMessage(new TextMessage(
				"{\"type\":\"connected\",\"message\":\"WebSocket linked — implement tick streaming here\"}"));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		// TODO: handle client messages if needed
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		// TODO: clean up session resources
	}
}
