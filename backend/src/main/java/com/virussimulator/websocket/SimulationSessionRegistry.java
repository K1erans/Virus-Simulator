package com.virussimulator.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virussimulator.simulation.dto.SnapshotMessage;

@Component
public class SimulationSessionRegistry {

	private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
	private final ObjectMapper objectMapper;

	public SimulationSessionRegistry(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public void register(WebSocketSession session) {
		sessions.add(session);
	}

	public void unregister(WebSocketSession session) {
		sessions.remove(session);
	}

	public void send(WebSocketSession session, SnapshotMessage message) throws IOException {
		if (!session.isOpen()) {
			unregister(session);
			return;
		}

		synchronized (session) {
			session.sendMessage(new TextMessage(toJson(message)));
		}
	}

	public void broadcast(SnapshotMessage message) {
		String payload = toJson(message);
		for (WebSocketSession session : sessions) {
			if (!session.isOpen()) {
				unregister(session);
				continue;
			}

			try {
				synchronized (session) {
					session.sendMessage(new TextMessage(payload));
				}
			} catch (IOException ex) {
				unregister(session);
			}
		}
	}

	private String toJson(SnapshotMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to serialize simulation message", ex);
		}
	}
}
