package com.harness.workshop.model;
import java.util.List;

public class EvalResult {
    private String flagKey;
    private boolean on;
    private int rolloutPercent;
    private double userBucket; // 0..100
    private List<String> matchedSegments;
    private List<String> excludedSegments;
    private String reason;

    public EvalResult() {}
    public EvalResult(String flagKey, boolean on, int rolloutPercent, double userBucket,
                      List<String> matchedSegments, List<String> excludedSegments, String reason) {
        this.flagKey = flagKey; this.on = on; this.rolloutPercent = rolloutPercent;
        this.userBucket = userBucket; this.matchedSegments = matchedSegments; this.excludedSegments = excludedSegments; this.reason = reason;
    }

    public String getFlagKey() {
        return flagKey;
    }

    public void setFlagKey(String flagKey) {
        this.flagKey = flagKey;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getRolloutPercent() {
        return rolloutPercent;
    }

    public void setRolloutPercent(int rolloutPercent) {
        this.rolloutPercent = rolloutPercent;
    }

    public double getUserBucket() {
        return userBucket;
    }

    public void setUserBucket(double userBucket) {
        this.userBucket = userBucket;
    }

    public List<String> getMatchedSegments() {
        return matchedSegments;
    }

    public void setMatchedSegments(List<String> matchedSegments) {
        this.matchedSegments = matchedSegments;
    }

    public List<String> getExcludedSegments() {
        return excludedSegments;
    }

    public void setExcludedSegments(List<String> excludedSegments) {
        this.excludedSegments = excludedSegments;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}