# 🧩 Workshop Guide: Feature Management & Experimentation (FME)

This workshop walks you through configuring environments, feature flags, and segments using the **Feature Management & Experimentation (FME)** module.  
By the end of the lab, you'll understand environment setup, targeting, rollouts, and resilience validation.

---

## 📚 Table of Contents

1. [Create Workshop Environment](#️-1-create-workshop-environment)
2. [Create a Feature Flag](#-2-create-a-feature-flag)
3. [Attribute-Based Targeting](#-3-attribute-based-targeting)
4. [Progressive Rollout](#-4-progressive-rollout)
5. [Segments](#-5-segments)
   - [Test Segment Targeting](#step-2-test-segment-targeting)
6. [Dynamic Segments](#-6-dynamic-segments)
7. [Experimentation with Metrics and Guardrails](#-7-experimentation-with-metrics-and-guardrails)
   - [Generate Experiment Traffic](#step-1-generate-experiment-traffic)
   - [Create Metrics](#step-2-create-metrics)
   - [Set Up Guardrail Metrics](#step-3-set-up-guardrail-metrics)
   - [Test Your Experiment](#step-4-test-your-experiment)
8. [Validate Experience During Outage (Advanced)](#-8-validate-experience-during-outage-advanced)
9. [Review and Wrap-Up](#-9-review-and-wrap-up)

---
## Lab Pre-Read

### Changing Harness Modules
As part of this lab, we will switch between modules several times.

**To switch modules:**
1. Click on the **nine dot** menu icon
2. Select the module relevant to the step
3. The lab begins in the code repository

<img width="362" height="193" alt="image" src="https://github.com/user-attachments/assets/2d953dd9-b370-4308-9504-a9bf13f7fb9a" />


## ⚙️ 1. Create Workshop Environment

### Step 1: Access Project Settings
1.  In the Harness UI, navigate to the **Feature Management & Experimentation** module
2.  From the **left-hand side menu**, navigate to **FME Settings**.
3.  From the new navigation bar select Projects
4.  Among all visible projects, click **View** (under Actions) for the one relevant to your user.
5.  You should see two environments we’ll use the **staging** environment for this lab




---

### Step 2: Obtain SDK Key
1. To generate an SDK token select the SDK API Keys tab
2. Select the staging environment(stg-) and copy server-side key

<img width="1287" height="335" alt="image" src="https://github.com/user-attachments/assets/e3f470e3-3ef3-4cf1-893f-6959a32dc881" />
---

### Step 3: Configure Project Variables
1. From the **left-hand side menu**, select **Project Settings → Variables**.  
2. Edit the variable `sdk_key`.  
3. In the **Fixed Value** input box, paste the key you copied earlier.  


> [!NOTE]
> For production deployments, store keys as secrets. For this lab, plain text is acceptable.

---

### Step 4: Deploy and Validate
1. From the Harness module selector select Continuous Delivery
2. From the **left-hand side menu**, select **Pipelines**
4. Run the **springboot-deploy** pipeline
5. After the pipeline completes successfully validate the app is running
6. From the **left-hand side menu**, select **Project Settings → Variables**.
7. The URL of the deployed application can be seen under **fme_url**
8. The application will be used to validate Feature Flag changes as we progress through the lab

<img width="1567" height="346" alt="image" src="https://github.com/user-attachments/assets/a608515f-362c-4747-bf48-8a7ad1b4a39a" />

9. The application allows to simulate users with different characteristics

> [!TIP]
> Pick a user and navigate through the available options
> Under the navigation bar you can see the attributed Feature Flags and an option to evaluate flags on demand

> [!NOTE]
> For the remaining of the lab we will not need to re-deploy the application while performing multiple releases


---

## 🚀 2. Create a Feature Flag

### Step 1: Create Feature Flag
1. From the module selection menu, select **Feature Management & Experimentation**.  
2. On the left-hand side menu, go to **Feature Flags → Create Feature Flag**.  
3. Fill in the following:

| Field | Value |
|--------|--------|
| **Name** | <pre>`target_country`</pre> |
| **Owners** | <pre>`All Project Users`</pre> |


4. Click **Create**
4. From the new menu select the **Stg** envronment and **Initiate Environment**
5. Click **Review changes** (default for now) and click **Save**.

---

### Step 2: Validate Flag
1. Navigate to the application and select/create a user 
   
<img width="700" height="300" alt="image" src="https://github.com/user-attachments/assets/7cfdd129-0d92-4063-97d7-2f64241006c1" />

3. confirm that the new feature flag **appears**.

<img width="1204" height="723" alt="image" src="https://github.com/user-attachments/assets/1154f743-eb75-41a2-84e4-52426d042027" />

---

## 🎯 3. Attribute-Based Targeting

Let’s target specific user attributes.

### Step 1: Add Targeting Rules
1. Go back to your feature flag `target_country`.  
2. Click **Add Attribute based targeting rules**.  
3. Configure the rule as follows:

| Condition | Action |
|------------|---------|
| If <pre>`country`</pre> is in list <pre>`UK`</pre> | Serve **On** |

<img width="1249" height="263" alt="image" src="https://github.com/user-attachments/assets/d0eb73f2-3edf-4e7f-8ab4-1a769125c953" />


4. Click **Review changes**
5. Validate the audit of the change (change diff)
6. Click **Save**.

---

### Step 2: Validate
1. In the browser app, navigate to the users tab and select user **Alice**.  
2. Navigate to **Evaluate Flag** → confirm the flag is **On** for Alice.  

> [!TIP]
> **Bonus:** Experiment with other attribute combinations!


---

## 📈 4. Progressive Rollout

### Step 1: Switch flag to a progressive rollout
1. Go to the feature flag configuration for `target_country`.  
2. Under **Targeting Rules**, change **Serve** to:  
   > “Distribute treatments as follows”

| Treatment | Percentage |
|------------|-------------|
| On | 50% |
| Off | 50% |

This simulates a **progressive rollout** of the feature.

### Step 2: Validate
1. In the browser app, navigate to the users tab and select users from the **UK**.  
2. Navigate to **Evaluate Flag** → check if the flag is **on** or **off**.

---

## 👥 5. Segments

### Step 1: Create Segment
1. From the **left-hand side menu**, select **Segments → Create Segment**.  
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | `beta_users` |
| **Segment Type** | Standard |
| **Traffic Type** | user |

3. Click **Create**
4. Ensure environment = **staging-user1**.
5. Click **Add definition**
6. Then **Add User**, click **Add Individually** and add users:  
   - `u001`  
   - `u006`  
7. Click **Save**.

<img width="600" height="500" alt="image" src="https://github.com/user-attachments/assets/496d2fd3-79cc-4f24-a7a9-630ba323098a" />



---

### Step 2: Test Segment Targeting
1. Go to **Feature Flags → Create Feature Flag**.  
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | `target_beta_users` |


3. Initiate the flag for the **staging** environment.  
4. Select **Add New Individual Target**:

| Description | To Segments |
|--------------|--------------|
| on | beta_users |


5. Review and **Save**.

6. In the app, select user **Alice**, click **Impersonate**, and navigate to **Evaluate**.  
   - Confirm that Alice (a beta user) receives the **On** treatment.

---

## 🔄 6. Dynamic Segments

### Step 1: Create Dynamic Segment
1. From the **left-hand side menu**, go to **Segments → Create Segment**.  
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | `pro_users_dynamic` |
| **Segment Type** | Rule-based |
| **Traffic Type** | user |

3. Under **Targeting Rules**, click **+ Add New Rule**:  
   - **If** `plan` is in list `pro`.

4. Review and **Save**.

---

### Step 2: Target Dynamic Segment
1. Go to **Feature Flags → Create Feature Flag**.  
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | `target_pro_users_segment` |

3. Initiate flag for **staging** environment.  
4. Add a new target:

| Description | To Segments |
|--------------|--------------|
| on | pro_users_dynamic |

5. Review and **Save**.

6. In the app, select any user with a **pro** plan → click **Impersonate** → navigate to **Evaluate** tab.  
   - Confirm the treatment is **On**.

---

## 🔬 7. Experimentation with Metrics and Guardrails

Now that you've created feature flags and targeting rules, let's set up **experimentation** to measure the impact of your feature on key business metrics.

### Step 1: Generate Experiment Traffic

Before setting up metrics, you need event data. This application includes a **traffic simulation** feature that generates synthetic impressions and events.

#### Option A: Manual Simulation (UI)

1. In the application, navigate to the **Simulate** page from the top navigation.
2. Click **"Simulate users & generate traffic"** button.
3. Wait for the simulation to complete (~1-2 minutes depending on flag count).
4. You should see progress indicators showing:
   - Users processed
   - Impressions registered (via `getTreatment`)
   - Events tracked (via `track()`)

#### Option B: Automatic Background Simulation

For continuous event generation, enable **scheduled background simulation**:

1. Edit your deployment configuration or `values.yaml`:
   ```yaml
   env:
     SIMULATION_SCHEDULED_ENABLED: "true"
     SIMULATION_SCHEDULED_FIXED_DELAY_MS: "5000"
     SIMULATION_SCHEDULED_MIN_PER_TREATMENT: "100"
   ```

2. Redeploy the application - simulation will run automatically every 5 seconds.

3. Monitor via API:
   ```bash
   curl "http://<<project_name>>.cie-demo.co.uk/api/simulate/status"
   ```

📖 **Learn more**: See [docs/SCHEDULED_SIMULATION.md](docs/SCHEDULED_SIMULATION.md) for configuration details.

#### Option C: API-Based Simulation

Trigger simulation via REST API:

```bash
# Start simulation (non-blocking)
curl -X POST "http://<<project_name>>.cie-demo.co.uk/api/simulate?minPerTreatment=100"

# Check status
curl "http://<<project_name>>.cie-demo.co.uk/api/simulate/status"
```

---

### Step 2: Create Metrics

Metrics let you measure the impact of feature flag treatments on business outcomes.

#### Built-in Event Types

This application tracks these events automatically:

| Event Type | Description | Expected Impact |
|------------|-------------|-----------------|
| `feature.evaluated` | Flag evaluation event | ✅ Positive (on > off) |
| `user.login` | User login event | ❌ Negative (on < off) - demonstrates regression detection |
| `user.impersonated` | User impersonation action | ✅ Positive (on > off) |
| `feature.dashboard_viewed` | Dashboard view event | ⚪ Inconclusive (on ≈ off) |

#### Create a Metric

1. From Harness FME, navigate to **Metrics** → **Create Metric**.
2. Fill in the following:

   | Field | Value |
   |-------|-------|
   | **Name** | `Feature Evaluation Success` |
   | **Event Type** | `feature.evaluated` |
   | **Aggregation** | **Count** (counts total events) |
   | **Traffic Type** | `user` |

3. Click **Save**.

4. **Repeat** to create additional metrics:
   - **Name**: `User Login Rate`  
     **Event**: `user.login`  
     **Aggregation**: Count
   
   - **Name**: `Dashboard Engagement`  
     **Event**: `feature.dashboard_viewed`  
     **Aggregation**: Count

💡 **Tip**: Event values are pre-calibrated in this workshop to produce different experiment outcomes (positive, negative, inconclusive) for learning purposes.

---

### Step 3: Set Up Guardrail Metrics

**Guardrail metrics** protect against regressions by automatically flagging experiments where a key metric deteriorates significantly.

#### What Are Guardrails?

Guardrails ensure that while testing a new feature:
- **Primary metrics** improve (e.g., feature adoption)
- **Guardrail metrics** don't regress (e.g., login success rate, error rate)

If a guardrail threshold is breached, you'll be alerted to investigate or halt the rollout.

#### Create a Guardrail Metric

1. Go to **Metrics** → **Create Metric**.
2. Configure:

   | Field | Value |
   |-------|-------|
   | **Name** | `Login Success Guardrail` |
   | **Event Type** | `user.login` |
   | **Aggregation** | **Sum** (sum of event values) |
   | **Traffic Type** | `user` |
   | **Guardrail** | ✅ **Enable** |
   | **Threshold** | `-20%` (alert if metric drops more than 20%) |

3. Click **Save**.

💡 **Note**: The `user.login` event is configured to show **negative impact** when the feature is "on" (on=45.0 vs off=95.0) — this demonstrates guardrail detection in action!

#### Best Practices for Guardrails

- **Core Experience Metrics**: Page load time, error rate, crash rate
- **Engagement Metrics**: Session duration, bounce rate
- **Business Metrics**: Conversion rate, revenue per user
- **Set Realistic Thresholds**: -10% to -30% depending on metric criticality

---

### Step 4: Test Your Experiment

Now let's attach metrics to a feature flag and run an experiment!

#### Attach Metrics to a Flag

1. Go to **Feature Flags** → Select `target_country` (or another flag with 50/50 rollout).
2. Navigate to the **Metrics** tab.
3. Click **Add Metrics**.
4. Select:
   - ✅ `Feature Evaluation Success` (primary metric)
   - ✅ `Login Success Guardrail` (guardrail)
   - ✅ `Dashboard Engagement` (secondary metric)
5. Click **Save**.

#### Run Traffic Simulation

If not already running, trigger simulation:

1. **Via UI**: Navigate to `/simulate` and click "Simulate users & generate traffic"
2. **Via API**: 
   ```bash
   curl -X POST "http://<<project_name>>.cie-demo.co.uk/api/simulate?minPerTreatment=350"
   ```
3. Wait for **≥350 samples per treatment** (on and off).

#### View Experiment Results

1. Go to **Feature Flags** → `target_country` → **Metrics** tab.
2. After **5-10 minutes** (event processing pipeline delay), you'll see:

   **Expected Results:**
   
   | Metric | Treatment | Result |
   |--------|-----------|--------|
   | `Feature Evaluation Success` | **On** | ✅ **+900% lift** (positive impact) |
   | `Feature Evaluation Success` | **Off** | Baseline |
   | `Login Success Guardrail` | **On** | ❌ **-53% drop** 🚨 **GUARDRAIL ALERT** |
   | `Login Success Guardrail` | **Off** | Baseline |
   | `Dashboard Engagement` | **On** | ⚪ **+2.5% lift** (inconclusive) |
   | `Dashboard Engagement` | **Off** | Baseline |

3. **Guardrail Alert**: Notice the login metric triggered a guardrail warning! This indicates the feature may be causing login issues and warrants investigation.

#### Interpret Results

- **Positive Lift**: Feature shows improvement → consider wider rollout
- **Negative Impact with Guardrail Alert**: Feature is causing regressions → investigate or rollback
- **Inconclusive**: Not enough data or no significant difference → continue monitoring or increase sample size

#### Verify Events in Harness

1. Navigate to **Admin Settings** → **Event Types**.
2. Confirm you see:
   - `feature.evaluated`
   - `user.login`
   - `feature.dashboard_viewed`
   - `user.impersonated`
3. Click on each event to see recent event data.

---

### 🧠 Discussion Questions

1. **Why did the login metric show negative impact?**  
   *Hint: Check event value configuration in [FmeEventService.java](application/src/main/java/com/harness/workshop/service/FmeEventService.java) — values are intentionally skewed for training.*

2. **When would you halt a rollout based on guardrail metrics?**  
   *Consider: severity of impact, affected user percentage, alternative solutions.*

3. **How many samples do you need for statistical significance?**  
   *Rule of thumb: ≥350 per treatment for most A/B tests; higher for smaller effect sizes.*

4. **What's the difference between primary and guardrail metrics?**  
   *Primary: what you're trying to improve; Guardrail: what you must not break.*

---

### 📊 Next Steps

- Create custom metrics for your own event types
- Experiment with different aggregations (Count, Sum, Average)
- Set up multiple guardrails for comprehensive protection
- Use segments to analyze metric impact by user cohort
- Integrate with alerting (Slack, PagerDuty) for guardrail breaches

💡 **Pro Tip**: In production, start with **guardrails on critical flows** (authentication, payment, core features) before experimenting with new features.

---

## 💥 8. Validate Experience During Outage (Advanced)

### Step 1: Create a Chaos Experiment
1. Duplicate your browser window.  
2. From the module selection menu, navigate to **Chaos Engineering**.
3. From the left hand side module, select the prebuild chaos experiment **fme-springboot-network-isolation**
4. Start the experiment
5. It may take a few seconds before the experiment is in effect

---

### Step 2: Observe SDK Behavior
1. While the experiment runs, in the second tab open `target_beta_users` feature flag.  
2. Toggle between **On** and **Off**.  
3. When the chaos experiment fully targets the pod, notice:  
   - The SDK **maintains the same flag treatment** (expected behavior).  
   - This demonstrates resilience — continued experience even when the Split.io platform is unreachable.

---

## 🎓 9. Review and Wrap-Up

### ✅ You’ve completed the Feature Management & Experimentation Workshop!

You’ve now explored:
- Environment setup and SDK key configuration  
- Creating and targeting feature flags  
- Attribute-based and segment-based targeting  
- Progressive rollouts and percentage-based rollouts
- Static and dynamic segments
- **Experimentation with metrics and guardrails** ✨
- Automated and scheduled traffic simulation
- Chaos testing and SDK resilience

🎉 **Great job!**

---

## Repository structure

| Path | Purpose |
|------|---------|
| `application/` | Spring Boot workshop app with Harness FME Java SDK |
| `manifests/` | Kubernetes deployment templates |
| `values.yaml` | Harness CD service values (SDK key, event tracking env) |

---

## Event tracking and experimentation

This repo sends **Harness FME events** via the Java SDK [`track()`](https://developer.harness.io/docs/feature-management-experimentation/release-monitoring/events/setup) method so you can build **metrics** and measure **experiment impact**.

### Built-in events

| Event type | When fired | Expected Experiment Impact |
|------------|------------|---------------------------|
| `feature.evaluated` | Each `getTreatment` evaluation (includes split, treatment, plan, country) | ✅ **POSITIVE** - on=1.0 vs off=0.1 |
| `user.login` | User signs in | ❌ **NEGATIVE** - on=45.0 vs off=95.0 (regression) |
| `user.impersonated` | User impersonation | ✅ **POSITIVE** - on=65.0 vs off=15.0 |
| `feature.dashboard_viewed` | Dashboard load | ⚪ **INCONCLUSIVE** - on=8.2 vs off=8.0 (no sig. diff) |

Events use traffic type **`user`** — the same key as flag evaluations — so metrics attribute correctly to treatments.

**Note**: Event values are calibrated to produce **varied experiment outcomes** (positive, negative, and inconclusive) for workshop demonstration purposes.

### Custom events API

```bash
curl -X POST "http://localhost:8080/api/events/u001" \
  -H "Content-Type: application/json" \
  -d '{"eventType":"checkout.completed","value":99.99,"properties":{"plan":"pro"}}'
```

### Configure metrics in Harness FME

1. Open **Feature Management & Experimentation** → **Metrics** → **Create metric**.
2. Choose event type `feature.evaluated` (or a custom type from above).
3. Attach the metric to a feature flag experiment and review **Metric impact** after events flow (~5 min pipeline delay).

Confirm events in **Admin settings → Event types**.

See **[docs/FME_METRICS.md](docs/FME_METRICS.md)** for which aggregation (Count / Sum / Average) to use per event type.

### Verify impressions (getTreatment)

```bash
curl "http://localhost:8080/api/diagnostics/impressions?userId=u001"
```

Each flag should return `on` or `off`, not `control`. Impressions are buffered by the SDK (~5s with `split.impressionsRefreshRate=5`).

### Build and run locally (one command)

```bash
cp application/.env.example application/.env
# Edit application/.env — set SPLIT_SDK_KEY to your staging server-side key

./run-local.sh
```

If it fails, run diagnostics:

```bash
./run-local.sh doctor
```

Other modes:

```bash
./run-local.sh jar       # build JAR, then run it
./run-local.sh build     # compile only
./run-local.sh test-api  # API simulation test (needs SPLIT_SDK_KEY)
./run-local.sh test-ui   # Selenium test (needs Chrome + SPLIT_SDK_KEY)
```

Manual run:

```bash
cd application
export SPLIT_SDK_KEY="your-server-side-sdk-key"
./mvnw spring-boot:run
```

### Deploy

Set Harness project variable `sdk_key`, then run the **springboot-deploy** pipeline. Event tracking env vars are injected from `values.yaml`.

---

## Simulate experiment traffic (350+ per treatment)

Use the **Simulate** page (or API) to generate traffic with a strict **impressions → delay → events** sequence per metric:

1. `getTreatment` on all flags (registers impressions)
2. Wait ~3 seconds (`simulation.eventDelayMs`)
3. Send that metric’s `track` event — **`on`** uses positive values, **`off`** uses negative/baseline values

Steps repeat for each event type in order: `feature.evaluated`, `user.login`, `feature.dashboard_viewed`, `user.impersonated`.

Continues until **≥350** samples per `on` and `off` per metric per flag.

### Scheduled Background Simulation (NEW ✨)

Run simulations automatically as a **background thread** every N seconds without blocking the application:

```properties
# Enable scheduled background simulation
simulation.scheduled.enabled=true
simulation.scheduled.fixedDelayMs=5000
simulation.scheduled.minPerTreatment=350
```

The service starts 10 seconds after JVM startup and runs every 5 seconds (configurable). Memory-optimized: only one simulation runs at a time.

📖 **See [docs/SCHEDULED_SIMULATION.md](docs/SCHEDULED_SIMULATION.md)** for full configuration and monitoring guide.

### Manual Simulation (UI)

1. Log in at `/` (pick any demo user).
2. Open **Simulate** from the nav or dashboard.
3. Click **Simulate users & generate traffic**.

### Manual Simulation (API)

```bash
# Background (poll status)
curl -X POST "http://localhost:8080/api/simulate?minPerTreatment=350"
curl "http://localhost:8080/api/simulate/status"

# Blocking (waits until complete)
curl -X POST "http://localhost:8080/api/simulate?blocking=true&minPerTreatment=350"
```

### Selenium integration test

Requires `SPLIT_SDK_KEY` and Chrome:

```bash
cd application
export SPLIT_SDK_KEY="your-staging-key"
./mvnw test -Dtest=TrafficSimulationSeleniumIT
```

Non-UI API test:

```bash
./mvnw test -Dtest=TrafficSimulationApiIT
```

> Flags must use **on/off** rollouts (e.g. 50/50). A 100% on rollout cannot reach 350 `off` impressions.
