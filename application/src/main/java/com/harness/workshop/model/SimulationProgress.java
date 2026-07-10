package com.harness.workshop.model;

import java.util.Map;

public class SimulationProgress {
    private final boolean running;
    private final int syntheticUsers;
    private final int minPerTreatment;
    private final int totalImpressions;
    private final int totalEvents;
    private final boolean targetsMet;
    private final String message;
    private final Map<String, Map<String, Integer>> impressionsBySplitAndTreatment;
    private final SimulationResult lastResult;

    public SimulationProgress(
            boolean running,
            int syntheticUsers,
            int minPerTreatment,
            int totalImpressions,
            int totalEvents,
            boolean targetsMet,
            String message,
            Map<String, Map<String, Integer>> impressionsBySplitAndTreatment,
            SimulationResult lastResult) {
        this.running = running;
        this.syntheticUsers = syntheticUsers;
        this.minPerTreatment = minPerTreatment;
        this.totalImpressions = totalImpressions;
        this.totalEvents = totalEvents;
        this.targetsMet = targetsMet;
        this.message = message;
        this.impressionsBySplitAndTreatment = impressionsBySplitAndTreatment;
        this.lastResult = lastResult;
    }

    public boolean isRunning() { return running; }
    public int getSyntheticUsers() { return syntheticUsers; }
    public int getMinPerTreatment() { return minPerTreatment; }
    public int getTotalImpressions() { return totalImpressions; }
    public int getTotalEvents() { return totalEvents; }
    public boolean isTargetsMet() { return targetsMet; }
    public String getMessage() { return message; }
    public Map<String, Map<String, Integer>> getImpressionsBySplitAndTreatment() {
        return impressionsBySplitAndTreatment;
    }
    public SimulationResult getLastResult() { return lastResult; }
}
