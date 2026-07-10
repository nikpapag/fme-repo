package com.harness.workshop.service;

import com.harness.workshop.model.SimulationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Background scheduled service that runs traffic simulation periodically.
 * Executes on a fixed delay after JVM startup and continues running every N seconds.
 * Memory-optimized: only one simulation runs at a time, respects the TrafficSimulationService lock.
 */
@Service
public class ScheduledSimulationService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledSimulationService.class);

    private final TrafficSimulationService simulationService;
    private final boolean scheduledEnabled;
    private final int minPerTreatment;
    private final int initialDelayMs;
    private final int fixedDelayMs;

    public ScheduledSimulationService(
            TrafficSimulationService simulationService,
            @Value("${simulation.scheduled.enabled:false}") boolean scheduledEnabled,
            @Value("${simulation.scheduled.minPerTreatment:350}") int minPerTreatment,
            @Value("${simulation.scheduled.initialDelayMs:10000}") int initialDelayMs,
            @Value("${simulation.scheduled.fixedDelayMs:5000}") int fixedDelayMs) {
        this.simulationService = simulationService;
        this.scheduledEnabled = scheduledEnabled;
        this.minPerTreatment = minPerTreatment;
        this.initialDelayMs = initialDelayMs;
        this.fixedDelayMs = fixedDelayMs;
    }

    @PostConstruct
    public void logConfiguration() {
        if (scheduledEnabled) {
            log.info("Scheduled simulation ENABLED - will run every {}ms after {}ms initial delay, minPerTreatment={}",
                    fixedDelayMs, initialDelayMs, minPerTreatment);
        } else {
            log.info("Scheduled simulation DISABLED - use simulation.scheduled.enabled=true to enable");
        }
    }

    /**
     * Runs after application startup is complete, then repeats on fixed delay.
     * Uses fixedDelayString to allow property-based configuration.
     * Memory-safe: skips execution if a simulation is already running.
     */
    @Scheduled(
            initialDelayString = "${simulation.scheduled.initialDelayMs:10000}",
            fixedDelayString = "${simulation.scheduled.fixedDelayMs:5000}"
    )
    public void runScheduledSimulation() {
        if (!scheduledEnabled) {
            return;
        }

        if (simulationService.isRunning()) {
            log.debug("Scheduled simulation skipped - another simulation is already running");
            return;
        }

        try {
            log.info("Starting scheduled simulation with minPerTreatment={}", minPerTreatment);
            SimulationResult result = simulationService.startSimulation(minPerTreatment);
            log.info("Scheduled simulation initiated: status={}, message={}",
                    result.getStatus(), result.getMessage());
        } catch (Exception e) {
            log.error("Scheduled simulation failed to start", e);
        }
    }

    /**
     * Optional: run one simulation immediately after application is ready.
     * Disabled by default to avoid conflicts with manual triggers during development.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        boolean runOnStartup = Boolean.parseBoolean(
                System.getProperty("simulation.runOnStartup", "false"));

        if (scheduledEnabled && runOnStartup) {
            log.info("Application ready - triggering initial simulation");
            try {
                Thread.sleep(initialDelayMs);
                runScheduledSimulation();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Startup simulation interrupted", e);
            }
        }
    }
}
