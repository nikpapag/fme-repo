package com.harness.workshop;

import com.harness.workshop.model.SimulationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrafficSimulationApiIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void blockingSimulationReachesTargets() {
        assumeTrue(
                System.getenv("SPLIT_SDK_KEY") != null && !System.getenv("SPLIT_SDK_KEY").isBlank(),
                "SPLIT_SDK_KEY required");

        String url = "http://localhost:" + port + "/api/simulate?blocking=true&minPerTreatment=350";
        ResponseEntity<SimulationResult> response = rest.postForEntity(url, null, SimulationResult.class);

        assertNotNull(response.getBody());
        SimulationResult body = response.getBody();
        assertTrue(
                "COMPLETE".equals(body.getStatus()) || "PARTIAL".equals(body.getStatus()),
                "Unexpected status: " + body.getStatus() + " — " + body.getMessage());
        assertTrue(body.getSyntheticUsers() > 0, "Should process synthetic users");
        assertTrue(body.getTotalImpressions() > 0, "Should generate impressions");
        assertTrue(body.getTotalEvents() > 0, "Should generate events");
    }
}
