package com.virussimulator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virussimulator.simulation.dto.SimulationStatusResponse;
import com.virussimulator.simulation.model.SimulationConfig;
import com.virussimulator.simulation.service.SimulationService;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

	private final SimulationService simulationService;

	public SimulationController(SimulationService simulationService) {
		this.simulationService = simulationService;
	}

	@PostMapping("/start")
	public SimulationStatusResponse start(@RequestBody(required = false) SimulationConfig config) {
		return simulationService.start(config);
	}

	@PostMapping("/pause")
	public SimulationStatusResponse pause() {
		return simulationService.pause();
	}

	@PostMapping("/reset")
	public SimulationStatusResponse reset() {
		return simulationService.reset();
	}

	@GetMapping("/config")
	public SimulationConfig config() {
		return simulationService.getConfig();
	}
}
