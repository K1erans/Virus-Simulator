package com.virussimulator.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

	private static final Map<String, String> NOT_IMPLEMENTED = Map.of(
			"message", "Not implemented yet — add your simulation logic here");

	@PostMapping("/start")
	public ResponseEntity<Map<String, String>> start(@RequestBody(required = false) Map<String, Object> config) {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(NOT_IMPLEMENTED);
	}

	@PostMapping("/pause")
	public ResponseEntity<Map<String, String>> pause() {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(NOT_IMPLEMENTED);
	}

	@PostMapping("/reset")
	public ResponseEntity<Map<String, String>> reset() {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(NOT_IMPLEMENTED);
	}

	@GetMapping("/config")
	public ResponseEntity<Map<String, String>> config() {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(NOT_IMPLEMENTED);
	}
}
