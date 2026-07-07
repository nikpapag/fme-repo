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
7. [Validate Experience During Outage (Advanced)](#-7-validate-experience-during-outage-advanced)
8. [Review and Wrap-Up](#-8-review-and-wrap-up)

---

## ⚙️ 1. Create Workshop Environment

### Step 1: Access Project Settings
1.  From the **left-hand side menu**, navigate to **FME Settings**.
2.  Among all visible projects, click **View** for the one relevant to your user.
3. You should see two environments — by default, we’ll use the **staging** environment for this lab.

<img width="783" height="191" alt="image" src="https://github.com/user-attachments/assets/097c1f2b-6815-41e0-ab39-28bc0851057d" />

---

### Step 2: Obtain SDK Key
1. From the **top navigation bar**, select **SDK API Keys**.
2. Copy the **server-side key** for the **staging environment**.

---

### Step 3: Configure Project Variables
1. From the **left-hand side menu**, select **Project Settings → Variables**.  
2. Edit the variable `sdk_key`.  
3. In the **Fixed Value** input box, paste the key you copied earlier.  

> 💡 **Note:** For production deployments, store keys as secrets. For this lab, plain text is acceptable.

---

### Step 4: Deploy and Validate
1. Deploy your application.
2. From the Harness module selector select Continuous Delivery
3. From the **left-hand side menu**, select **Pipelines**
4. Run the **springboot-deploy** pipeline
5. Once deployed, navigate to the application, url of the app: http://<<project_name>>.cie-demo.co.uk 
6. Select one of the predefined **users**, and view the list of available features.  

🧠 **Question:** What do you notice about the available features?

---

## 🚀 2. Create a Feature Flag

### Step 1: Create Feature Flag
1. From the module selection menu, select **Feature Management & Experimentation**.  
2. On the right-hand side menu, go to **Feature Flags → Create Feature Flag**.  
3. Fill in the following:

| Field | Value |
|--------|--------|
| **Name** | <pre>`target_country`</pre> |
| **Owners** | <pre>`All Project Users`</pre> |

4. Select the **staging** environment and click **Initiate Environment**.  
5. Review changes and click **Save**.

---

### Step 2: Validate Flag
1. Navigate to the application and select/create a user 
   
<img width="700" height="300" alt="image" src="https://github.com/user-attachments/assets/7cfdd129-0d92-4063-97d7-2f64241006c1" />

3. confirm that the new feature flag **appears**.

<img width="700" height="400" alt="image" src="https://github.com/user-attachments/assets/32a7d2db-bbdf-4b1e-a4b7-eca8a19aad5e" />


---

## 🎯 3. Attribute-Based Targeting

Let’s target specific user attributes.

### Step 1: Add Targeting Rules
1. Go back to your feature flag `target_country`.  
2. Click **Add Attribute-Based Targeting Rules**.  
3. Configure the rule as follows:

| Condition | Action |
|------------|---------|
| If <pre>`country`</pre> is in list <pre>`UK`</pre> | Serve **On** |

4. Review changes and click **Save**.

<img width="650" height="370" alt="image" src="https://github.com/user-attachments/assets/731f41d3-69c9-42cb-b96b-0d3dd7e97362" />

---

### Step 2: Validate
1. In the browser app, navigate to the users tab and select user **Alice**.  
2. Navigate to **Evaluate Flag** → confirm the flag is **On** for Alice.  

💡 **Bonus:** Experiment with other attribute combinations!

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

3. Ensure environment = **staging-user1**.  
4. Under **User Section**, click **Add Individually** and add users:  
   - `u001`  
   - `u006`  
5. Click **Save**.

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

## 💥 7. Validate Experience During Outage (Advanced)

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

### ✅ You’ve completed the Feature Management & Experimentation Workshop!

You’ve now explored:
- Environment setup and SDK key configuration  
- Creating and targeting feature flags  
- Attribute-based and segment-based targeting  
- Progressive rollouts  
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

### UI

1. Log in at `/` (pick any demo user).
2. Open **Simulate** from the nav or dashboard.
3. Click **Simulate users & generate traffic**.

### API

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
