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

## ⚙️ 1. Create Workshop Environment

### Step 1: Access Project Settings
1.  From the **left-hand side menu**, navigate to **FME Settings**.
2.  Among all visible projects, click **View** for the one relevant to your user.
3. You should see two environments — by default, we’ll use the relevant **staging** environment for this lab.

<img width="783" height="191" alt="image" src="https://github.com/user-attachments/assets/097c1f2b-6815-41e0-ab39-28bc0851057d" />

---

### Step 2: Obtain SDK Key
1. From the **top navigation bar**, select **SDK API Keys**.
2. Copy the **server-side key** for the respective **staging environment**.

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
| If <pre>`country`</pre> is in list <pre>`UK`</pre> | Serve: **On** |

💡 **Note**: Using a 50/50 distribution on the main targeting rule (instead of 100% On) ensures both treatments get traffic, which is necessary for experiment results and reduces the time to see meaningful data. Currently, it is set to 100% On for users in the UK.

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
2. Under the **ELSE** block for the **Targeting Rules**, change **Serve** to:  
   > “Distribute treatments as follows”

| Treatment | Percentage |
|------------|-------------|
| On | 50% |
| Off | 50% |

This simulates a **progressive rollout** of the feature.

### Step 2: Validate
1. In the browser app, navigate to the users tab and select users from any location.  
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
| **Owners** | All Project Users |

3. Ensure environment = Your relevant **Staging** environment.  
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
| **Traffic Type** | `user` |
| **Owners** | `All Project Users` |


3. Initiate the flag for the relevant **staging** environment.  
4. Select **Add New Individual Target**:

| Description | To Segments |
|--------------|--------------|
| on | `beta_users` |


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
| **Owners** | `All Project Users` |

3. Under **Targeting Rules**, click **+ Add New Rule**:  
   - **If** `plan` is in list `pro`.

4. Review and **Save**.

💡 **Note**: You have to type `plan` and press enter in the left empty box and then `pro` and press enter on the right hand side.

---

### Step 2: Target Dynamic Segment
1. Go to **Feature Flags → Create Feature Flag**.  
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | `target_pro_users_segment` |
| **Traffic Type** | `user` |
| **Owners** | `All Project Users` |

3. Initiate flag for **staging** environment.  
4. Add a new target:

| Description | To Segments |
|--------------|--------------|
| on | `pro_users_dynamic` |

5. Review and **Save**.

6. In the app, select any user with a **pro** plan → click **Impersonate** → navigate to **Evaluate** tab.  
   - Confirm the treatment is **On**.

---

## 🔬 7. Experimentation with Metrics and Guardrails

Now that you've created feature flags and targeting rules, let's set up **experimentation** to measure the impact of your feature on key business metrics.

### Step 1: Generate Experiment Traffic

Before setting up metrics, you need event data. This application includes a **traffic simulation** feature that generates synthetic impressions and events.

#### Manual Simulation (UI)

1. In the application, navigate to the **Simulate** page from the top navigation.
2. Click **"Simulate users & generate traffic"** button.
3. Wait for the simulation to complete (~1-2 minutes depending on flag count).
4. You should see progress indicators showing:
   - Users processed
   - Impressions registered (via `getTreatment`)
   - Events tracked (via `track()`)

5. **Verify Data Flow**: Open **Data Hub Live Tail** (two browser tabs) for both impressions and events to confirm they are flowing in for your environment:
   - Navigate to **Data Hub** in Harness FME
   - Select **Live Tail** 
   - Filter by your relevant **Staging** environment to see real-time impressions (first tab) and events (second tab)
   - Notice your impressions and events flowing into the Platform

💡 **Note**: In case you are not receiving any traffic for impressions and events, ensure you have selected the correct and relevant **Staging** environment.

---

### Step 2: Create Metrics

Metrics let you measure the impact of feature flag treatments on business outcomes.

#### Built-in Event Types

This application tracks these events automatically:

| Event Type | Description | Desired Impact Direction |
|------------|-------------|--------------------------|
| `feature.evaluated` | Flag evaluation event | ✅ **Increase** (on > off) |
| `user.login` | User login event | ❌ **Decrease** (on < off) - demonstrates regression detection |
| `user.impersonated` | User impersonation action | ✅ **Increase** (on > off) |
| `feature.dashboard_viewed` | Dashboard view event | ⚪ **Inconclusive** (on ≈ off) |

#### Create a Metric

1. From Harness FME, navigate to **Metrics** → **Create Metric**.
2. Fill in the following:

   | Field | Value |
   |-------|-------|
   | **Name** | `Feature Evaluation Success` |
   | **Metric Category** | **None** |
   | **Select Desired Impact** | **Increase** (positive) |
   | **Event Type** | `feature.evaluated` |
   | **Aggregation** | **Count of events per user** (counts total events) |
   | **Traffic Type** | `user` |
   

3. Click **Save**.

4. **Repeat** to create additional metrics:
   - **Name**: `User Login Rate`  
     **Event**: `user.login`  
     **Aggregation**: Count  
     **Selected Desired Impact**: **Increase** (positive)
   
   - **Name**: `Dashboard Engagement`  
     **Event**: `feature.dashboard_viewed`  
     **Aggregation**: Count  
     **Select Desired Impact**: **Increase** (positive)

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
   | **Metric Category** | **Guardrail metric** ⚠️ *Must be set to "Guardrail metric" to appear under Guardrail metrics* |
   | **Select Desired Impact** | **Increase** (positive) |

3. Once the Guardrail metric is created, click on **Alert Policy** then **Create Alert Policy** and configure as follows:
| Field | Value |
   |-------|-------|
   | **Name** | `Login Success Degraded` |
   | **Choose your environment** | Relevant `Staging Environment` |
   | **Relevant Difference** | `20%` (alert if metric drops more than 20%) ⚠️ *Note: Use positive value only - negative values not supported* |
   | **Define alert notification channel** | `Leave as default` |

4. Click **Save**.

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
6. Click **"Recalculate All Metrics"** to ensure all metric calculations are up to date.


💡 **While waiting (5-10 minutes)**: Review your experiment settings to ensure everything is configured correctly. Verify:
   - Flag targeting rules are set properly
   - Metrics are attached correctly
   - Environment settings match your expectations
   - Navigate to **FME Settings** on the left, under the **Experimentation settings** section, click on **Monitor window and statistics**
   - Ensure the following is configured:

| Field | Value |
|-------|-------|
| **Monitor window** | `24 hours` |
| **Monitor significance threshold** | `0.05` |
| **Testing Method** | `Fixed Horizon` |
| **Default significance threshold** | `0.2` |
| **Minimum Sample size** | `10` |
| **Default power threshold** | `80` |
| **Experimental Review Period** | `1 Day` |
| **Multiple comparison correction** | ✅ |
   
   The default experiment settings are fine, but confirming helps ensure you know where to find these settings.
   
   **Note**: These Experiment Settings are only setup like this for this Hands-on lab. In production systems, please refer to your relevant teams to ensure appropriate settings are applied based on your business and industry.

#### View Experiment Results

1. Go to **Feature Flags** → `target_country` → **Metrics Impact** tab.
2. **Important**: Verify that the correct dropdown filter is selected: **"country is in list [UK]"**
3. Ensure a **minimum of 10 event entries have been registered for each treatment** (on and off) for calculations to appear.
4. After **5-10 minutes** (event processing pipeline delay), you'll see:

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

⚠️ **Note**: Initial results may show as "Inconclusive" during the first 10-15 minutes as events are still processing. With more data collection, the expected results above should appear. Results timing varies based on event throughput and processing pipeline latency.

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

