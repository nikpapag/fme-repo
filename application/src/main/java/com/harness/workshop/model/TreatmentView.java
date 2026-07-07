package com.harness.workshop.model;

public class TreatmentView {
    private final String split;
    private final String treatment;
    private final String configJson; // may be null on newer SDKs
    private final String label;      // may be null on newer SDKs

    public TreatmentView(String split, String treatment, String configJson, String label) {
        this.split = split;
        this.treatment = treatment;
        this.configJson = configJson;
        this.label = label;
    }

    public String getSplit() { return split; }
    public String getTreatment() { return treatment; }
    public String getConfigJson() { return configJson; }
    public String getLabel() { return label; }
}
