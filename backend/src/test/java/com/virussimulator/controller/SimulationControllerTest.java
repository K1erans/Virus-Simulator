package com.virussimulator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.virussimulator.simulation.service.SimulationService;

@SpringBootTest
@AutoConfigureMockMvc
class SimulationControllerTest {

	private static final String CONFIG_JSON = """
			{
				"populationSize": 50,
				"initialInfectedPct": 2.0,
				"transmissionRate": 0.3,
				"incubationPeriod": 4,
				"infectiousPeriod": 8,
				"recoveryRate": 0.12,
				"randomSeed": 7,
				"modelType": "Hybrid (Agent + SEIR)",
				"maxDays": 100
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SimulationService simulationService;

	@AfterEach
	void stopSimulation() {
		simulationService.pause();
	}

	@Test
	void startPauseResetAndConfigReturnSuccessfulResponses() throws Exception {
		mockMvc.perform(post("/api/simulation/start")
				.contentType(MediaType.APPLICATION_JSON)
				.content(CONFIG_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("started"))
				.andExpect(jsonPath("$.running").value(true));

		mockMvc.perform(get("/api/simulation/config"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.populationSize").value(50))
				.andExpect(jsonPath("$.transmissionRate").value(0.3));

		mockMvc.perform(post("/api/simulation/pause"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("paused"))
				.andExpect(jsonPath("$.running").value(false));

		mockMvc.perform(post("/api/simulation/reset"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("reset"))
				.andExpect(jsonPath("$.running").value(false));
	}
}
