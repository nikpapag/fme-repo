package com.harness.workshop.model;

import java.util.List;

public class FlagRule {
    // If user is in ANY of these segments => eligible before percentage rollout check
    private List<String> includeSegments;
    // If user is in ANY of these segments => exclude outright (overrides includes)
    private List<String> excludeSegments;

    // Percentage rollout 0..100 (e.g., 10 means 10%)
    private int rolloutPercent;

    public FlagRule() {}
    public FlagRule(List<String> includeSegments, List<String> excludeSegments, int rolloutPercent) {
        this.includeSegments = includeSegments; this.excludeSegments = excludeSegments; this.rolloutPercent = rolloutPercent;
    }

    public List<String> getIncludeSegments() {
        return includeSegments;
    }

    public void setIncludeSegments(List<String> includeSegments) {
        this.includeSegments = includeSegments;
    }

    public List<String> getExcludeSegments() {
        return excludeSegments;
    }

    public void setExcludeSegments(List<String> excludeSegments) {
        this.excludeSegments = excludeSegments;
    }

    public int getRolloutPercent() {
        return rolloutPercent;
    }

    public void setRolloutPercent(int rolloutPercent) {
        this.rolloutPercent = rolloutPercent;
    }
}
