# Metric Outcomes Configuration Guide

This document explains how the FME Workshop application has been configured to generate **realistic experiment outcomes** with varied results (positive, negative, and inconclusive).

## Overview

Based on real-world experimentation scenarios from the [Cortazar Split Scenarios](https://www.cortazar-split.com/scenarios.html), the application now generates four metrics with different statistical outcomes to demonstrate realistic A/B testing results.

## Configured Metrics

### 1. ✅ `feature.evaluated` - POSITIVE Impact

**Purpose**: Measure feature flag evaluation quality

**Event Values**:
- `on` treatment: `1.0`
- `off` treatment (baseline): `0.1`

**Expected Result**: 
- **10x improvement** with statistical significance
- P-value < 0.05
- Clear positive lift

**Use Case**: Demonstrates when a feature clearly improves a metric

**Recommended Aggregation**: AVERAGE

---

### 2. ❌ `user.login` - NEGATIVE Impact

**Purpose**: Measure user login engagement

**Event Values**:
- `on` treatment: `45.0`
- `off` treatment (baseline): `95.0`

**Expected Result**:
- **52% decrease** with statistical significance
- P-value < 0.05
- Clear negative impact

**Use Case**: Demonstrates when a feature causes regression - important for **kill switch decisions**

**Recommended Aggregation**: AVERAGE

**Workshop Learning**: This shows why you should **NOT ship** a feature that causes metrics to degrade.

---

### 3. ⚪ `feature.dashboard_viewed` - INCONCLUSIVE

**Purpose**: Measure dashboard engagement depth

**Event Values**:
- `on` treatment: `8.2`
- `off` treatment (baseline): `8.0`

**Expected Result**:
- **~2.5% difference** - too small to be significant
- P-value > 0.05
- No statistical significance

**Use Case**: Demonstrates when a feature has **no measurable impact** on a metric

**Recommended Aggregation**: AVERAGE

**Workshop Learning**: You may need more data, or this metric is simply not affected by the feature.

---

### 4. ✅ `user.impersonated` - POSITIVE Impact

**Purpose**: Measure user impersonation feature usage

**Event Values**:
- `on` treatment: `65.0`
- `off` treatment (baseline): `15.0`

**Expected Result**:
- **4.3x improvement** with statistical significance
- P-value < 0.05
- Clear positive lift

**Use Case**: Demonstrates strong feature adoption and positive impact

**Recommended Aggregation**: AVERAGE or COUNT

---

## How to Use in Workshop

### Step 1: Run Traffic Simulation

```bash
# Start simulation via UI
http://localhost:8080/simulate

# Or via API
curl -X POST "http://localhost:8080/api/simulate?minPerTreatment=350"
```

The simulation will generate:
- **≥350 impressions** per treatment per flag
- **Corresponding events** with the values above
- **3-second delay** between impressions and events

### Step 2: Create Metrics in Harness FME

1. Navigate to **FME → Metrics → Create metric**
2. Create metrics for each event type:

| Metric Name | Base Event Type | Aggregation | Expected Result |
|-------------|----------------|-------------|-----------------|
| Feature Evaluation Quality | `feature.evaluated` | AVERAGE | ✅ Positive |
| User Login Engagement | `user.login` | AVERAGE | ❌ Negative |
| Dashboard View Depth | `feature.dashboard_viewed` | AVERAGE | ⚪ Inconclusive |
| Impersonation Usage | `user.impersonated` | AVERAGE | ✅ Positive |

### Step 3: Attach Metrics to Experiment

1. Select a feature flag (e.g., `FME_Workshop_Simulation`)
2. Configure as a 50/50 A/B test
3. Attach all 4 metrics to the experiment
4. Wait ~5 minutes for pipeline processing

### Step 4: Review Results

Navigate to the flag's **Metric Impact** tab to see:
- ✅ Two metrics showing **positive impact**
- ❌ One metric showing **negative impact** (kill switch scenario)
- ⚪ One metric showing **no significant difference**

## Code Implementation

The metric values are configured in:
```
application/src/main/java/com/harness/workshop/service/FmeEventService.java
```

Key constants:
```java
private static final Map<String, Double> POSITIVE_VALUES = Map.of(
    EVENT_FEATURE_EVALUATED, 1.0,      // POSITIVE
    EVENT_USER_LOGIN, 45.0,             // NEGATIVE (lower than baseline)
    EVENT_FEATURE_DASHBOARD_VIEWED, 8.2, // INCONCLUSIVE
    EVENT_USER_IMPERSONATED, 65.0       // POSITIVE
);

private static final Map<String, Double> NEGATIVE_VALUES = Map.of(
    EVENT_FEATURE_EVALUATED, 0.1,       // Baseline
    EVENT_USER_LOGIN, 95.0,              // Baseline (on causes drop)
    EVENT_FEATURE_DASHBOARD_VIEWED, 8.0, // Baseline (similar)
    EVENT_USER_IMPERSONATED, 15.0        // Baseline
);
```

## Statistical Significance

Based on the configured values and minimum sample size of 355:

| Metric | Effect Size | Statistical Power | Significance |
|--------|-------------|-------------------|--------------|
| `feature.evaluated` | Very Large (10x) | ~100% | Will detect |
| `user.login` | Large (0.47x) | ~100% | Will detect |
| `feature.dashboard_viewed` | Negligible (1.025x) | ~5-10% | Won't detect |
| `user.impersonated` | Very Large (4.33x) | ~100% | Will detect |

## Learning Objectives

1. **Positive Metrics**: Understand when to confidently ship a feature
2. **Negative Metrics**: Learn when to kill or rollback a feature
3. **Inconclusive Metrics**: Understand when you need more data or the feature doesn't affect a metric
4. **Decision Making**: Practice interpreting mixed results (2 positive, 1 negative, 1 inconclusive)

## References

- [FME_METRICS.md](./FME_METRICS.md) - Detailed metric configuration guide
- [Cortazar Split Scenarios](https://www.cortazar-split.com/scenarios.html) - Real-world scenario examples
- [Harness FME Documentation](https://developer.harness.io/docs/feature-management-experimentation)
