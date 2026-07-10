# Harness FME metrics — aggregation per event

Impressions come **only** from `getTreatment()` (and `getTreatments*`). Events come from `track()`.  
Create metrics in **FME → Metrics → Create metric**, then attach them to your flag experiment.

Simulation sends these **event values** by treatment to produce **varied experiment outcomes**:

| Event Type | `on` Value | `off` Value | Expected Impact |
|------------|-----------|------------|-----------------|
| `feature.evaluated` | 1.0 | 0.1 | ✅ **POSITIVE** - Shows improvement |
| `user.login` | 45.0 | 95.0 | ❌ **NEGATIVE** - Shows regression |
| `feature.dashboard_viewed` | 8.2 | 8.0 | ⚪ **INCONCLUSIVE** - No significant difference |
| `user.impersonated` | 65.0 | 15.0 | ✅ **POSITIVE** - Shows improvement |

---

## Recommended aggregation by event

| Event type | Aggregation | Measure / setting | Expected Result | Why |
|------------|-------------|-------------------|-----------------|-----|
| `feature.evaluated` | **Average** | Value field | ✅ **POSITIVE** | Values are `1.0` (on) vs `0.1` (off) — shows clear lift in evaluation quality |
| `feature.evaluated` | **Count** (alt.) | Count of events | ✅ **POSITIVE** | Use if you only care how many evaluations happened |
| `user.login` | **Average** | Value field | ❌ **NEGATIVE** | Values are `45.0` (on) vs `95.0` (off) — shows treatment causes regression in engagement |
| `user.login` | **Sum** (alt.) | Value field | ❌ **NEGATIVE** | Total engagement volume will be lower for `on` treatment |
| `feature.dashboard_viewed` | **Average** | Value field | ⚪ **INCONCLUSIVE** | Values are `8.2` (on) vs `8.0` (off) — difference too small to be statistically significant |
| `user.impersonated` | **Count** | Count of events | ✅ **POSITIVE** | More impersonation events occur with `on` treatment |
| `user.impersonated` | **Average** | Value field | ✅ **POSITIVE** | Values are `65.0` (on) vs `15.0` (off) — shows increased usage intensity |

---

## Quick rules

- **Count** — “How many times did this happen?” (conversions, occurrences).
- **Sum** — “What is the total?” (revenue, total score, total sessions).
- **Average** — “What is the typical value per user?” (engagement score, depth, latency).

For this workshop’s **on vs off** simulation, prefer **Average** on the value field for `user.login` and `feature.dashboard_viewed`, and **Average** or **Count** for `feature.evaluated`.

## Expected Experiment Outcomes

When you run the simulation and create metrics for these events, you should see:

### ✅ Positive Impact Metrics
- **`feature.evaluated`**: `on` treatment shows **10x improvement** (1.0 vs 0.1)
  - Result: Statistically significant positive impact
  - Interpretation: The feature improves evaluation quality
  
- **`user.impersonated`**: `on` treatment shows **4.3x improvement** (65.0 vs 15.0)
  - Result: Statistically significant positive impact
  - Interpretation: The feature increases impersonation usage

### ❌ Negative Impact Metrics
- **`user.login`**: `on` treatment shows **52% decrease** (45.0 vs 95.0)
  - Result: Statistically significant **negative** impact
  - Interpretation: The feature causes login engagement to drop - **do not ship this!**

### ⚪ Inconclusive Metrics
- **`feature.dashboard_viewed`**: `on` and `off` are nearly identical (8.2 vs 8.0)
  - Result: **No statistically significant difference** (~2.5% difference)
  - Interpretation: The feature has no measurable impact on dashboard views
  - Action: Need more data or this metric is not affected by the feature

---

## Verify impressions in Harness

1. **Data Hub → Live tail** — filter traffic type `user`, see impressions after `getTreatment`.
2. Confirm logs do **not** show `control` for every call (means SDK key or flag setup issue).
3. After simulation, wait **~1× impressions refresh (5s) + pipeline (~5 min)** before metric impact updates.

---

## Troubleshooting missing impressions

| Symptom | Likely cause |
|---------|----------------|
| All treatments `control` | Wrong/missing `SPLIT_SDK_KEY`, SDK not `blockUntilReady`, or flag not initiated in staging |
| Impressions delayed 5+ min | Default SDK `impressionsRefreshRate` was 300s — now set to **5s** in `application.properties` |
| Events but no impressions | Only `track()` was called — impressions require `getTreatment()` first |
