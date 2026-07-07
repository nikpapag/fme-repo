package com.harness.workshop.model;


public class FeatureFlag {
    private String key;           // e.g., "new-dashboard"
    private String name;          // "New Dashboard"
    private String description;
    private boolean enabled;      // master switch
    private FlagRule rule;

    public FeatureFlag() {}
    public FeatureFlag(String key, String name, String description, boolean enabled, FlagRule rule) {
        this.key = key; this.name = name; this.description = description; this.enabled = enabled; this.rule = rule;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FlagRule getRule() {
        return rule;
    }

    public void setRule(FlagRule rule) {
        this.rule = rule;
    }
}
