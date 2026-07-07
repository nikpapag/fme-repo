package com.harness.workshop.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimulationResult {
    private final String status;
    private final String message;
    private final int syntheticUsers;
    private final int totalImpressions;
    private final int totalEvents;
    private final int minPerTreatment;
    private final long durationMs;
    private final Map<String, Map<String, Integer>> impressionsBySplitAndTreatment;
    private final Map<String, Map<String, Map<String, Integer>>> eventsByTypeSplitAndTreatment;

    public SimulationResult(
            String status,
            String message,
            int syntheticUsers,
            int totalImpressions,
            int totalEvents,
            int minPerTreatment,
            long durationMs,
            Map<String, Map<String, Integer>> impressionsBySplitAndTreatment,
            Map<String, Map<String, Map<String, Integer>>> eventsByTypeSplitAndTreatment) {
        this.status = status;
        this.message = message;
        this.syntheticUsers = syntheticUsers;
        this.totalImpressions = totalImpressions;
        this.totalEvents = totalEvents;
        this.minPerTreatment = minPerTreatment;
        this.durationMs = durationMs;
        this.impressionsBySplitAndTreatment = impressionsBySplitAndTreatment;
        this.eventsByTypeSplitAndTreatment = eventsByTypeSplitAndTreatment;
    }

    public static SimulationResult alreadyRunning() {
        return new SimulationResult(
                "RUNNING",
                "Simulation already in progress",
                0, 0, 0, 0, 0,
                Map.of(), Map.of());
    }

    public static SimulationResult noFlags() {
        return new SimulationResult(
                "FAILED",
                "No feature flags found. Check SPLIT_SDK_KEY and initiate flags in Harness FME.",
                0, 0, 0, 0, 0,
                Map.of(), Map.of());
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getSyntheticUsers() { return syntheticUsers; }
    public int getTotalImpressions() { return totalImpressions; }
    public int getTotalEvents() { return totalEvents; }
    public int getMinPerTreatment() { return minPerTreatment; }
    public long getDurationMs() { return durationMs; }
    public Map<String, Map<String, Integer>> getImpressionsBySplitAndTreatment() {
        return impressionsBySplitAndTreatment == null ? Map.of() : impressionsBySplitAndTreatment;
    }
    public Map<String, Map<String, Map<String, Integer>>> getEventsByTypeSplitAndTreatment() {
        return eventsByTypeSplitAndTreatment == null ? Map.of() : eventsByTypeSplitAndTreatment;
    }
}
