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
8. [Review and Wrap-Up](#-9-review-and-wrap-up)

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

### Overview
Configure environment-specific SDK keys and deploy the workshop application for testing feature flags.

### Value
Server-side SDKs cache flag rules locally for fast evaluation without network calls. Real-time updates via Stream Processor enable instant feature changes without redeployment


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

### Overview
Create your first feature flag and validate it in the application. Learn how flags decouple deployment from release.

### Value
Feature flags increase delivery velocity while reducing risk. Deploy code in smaller chunks and more often by merging feature branches sooner. Release coordination is simplified as business teams can control features independently without engineering involvement


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

### Overview
Target users based on custom attributes like country, subscription tier, or device type for context-aware feature delivery.

### Value
Enables flexible segmentation beyond user IDs for business-relevant rollouts. Uses deterministic hashing to ensure users get the same flag value consistently across all services 

### Step 1: Add Targeting Rules
1. Go back to your feature flag `target_country`.  
2. Click **Add Attribute based targeting rules**.  
3. Configure the rule as follows:

| Condition | Action |
|------------|---------|
| If <pre>`country`</pre> is in list <pre>`UK`</pre> | Serve: **On** |

💡 **Note**: Using a 50/50 distribution on the main targeting rule (instead of 100% On) ensures both treatments get traffic, which is necessary for experiment results and reduces the time to see meaningful data. Currently, it is set to 100% On for users in the UK.

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

### Overview
Gradually roll out features using percentage-based distribution (10% → 50% → 100%) to control exposure and monitor impact at each stage.

### Value
Mitigates risk by testing features incrementally before full release. Deterministic assignment ensures user continuity, original users maintain access as percentages increase. Enables QA validation without affecting production users

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

### Overview
Create static segments to manage groups of users in bulk, such as beta testers or internal teams, with both inclusion and exclusion capabilities.

### Value
Scalable bulk user management eliminates individual targeting overhead. Supports precision control where individual targets override group rules. Targets can represent users, applications, systems, or any uniquely identified resource 

### Step 1: Create Segment
1. From the **left-hand side menu**, select **Segments → Create Segment**.
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | `beta_users` |
| **Segment Type** | Standard |
| **Traffic Type** | user |
| **Owners** | All Project Users |

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
| **Name** | <pre>`target_beta_users`</pre> |
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
  
> [!WARNING]
> For the lab the SDK polls for flag changes every 30 seconds, the interval can be reduced from the SDK settings or even be switched to a push model


---

## 🔄 6. Dynamic Segments

### Overview
Create rule-based segments that automatically include targets matching specified conditions, eliminating manual user management.

### Value
Automates segmentation at scale. New users matching criteria are automatically included without manual intervention. Supports complex logic with AND rules for sophisticated targeting. Scales effortlessly as your user base grows

### Step 1: Create Dynamic Segment
1. From the **left-hand side menu**, go to **Segments → Create Segment**.  
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | <pre>`pro_users_dynamic`</pre> |
| **Segment Type** | Rule-based |
| **Traffic Type** | user |
| **Owners** | `All Project Users` |

3. Click **Add definition**
4. Under **Targeting Rules**, click **+ Add New Rule**:  
   - **If** `plan` is in list `pro`.

5. Review and **Save**.

💡 **Note**: You have to type `plan` and press enter in the left empty box and then `pro` and press enter on the right hand side.

---

### Step 2: Target Dynamic Segment
1. Go to **Feature Flags → Create Feature Flag**.  
2. Configure:

| Field | Value |
|--------|--------|
| **Name** | <pre>`target_pro_users_segment`</pre> |
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


### Overview
Set up A/B testing with standardised metrics and guardrail protection to measure feature impact on business outcomes and detect regressions automatically.

### Value
Self-service experimentation without specialist headcount. Every team can innovate at the pace of ideas. Auto capture performance metrics the moment gradual releases begin, detecting the impact of each individual feature even with concurrent releases. Guardrail metrics protect against negative impacts with instant alerts. AI-powered insights explain results and provide guided next steps 

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

📖 **Learn more**: See [docs/SCHEDULED_SIMULATION.md](docs/SCHEDULED_SIMULATION.md) for configuration details.

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

> [!WARNING]
> Leave all other settings with their default value

   | Field | Value |
   |-------|-------|
   | **Name** | <pre>`Feature Evaluation Success`</pre> |
   | **Owners** | All Project Users |
   | **Traffic Type** | `user` |
   | **Measure as** | **Count** (counts total events) |
   | **Event Type** | `feature.evaluated` |


4. Click **Create**.

5. **Repeat** to create additional metrics:


## User Login Rate

   | Field | Value |
   |-------|-------|
   | **Name** | <pre>`User Login Rate`</pre> |
   | **Owners** | All Project Users |
   | **Traffic Type** | `user` |
   | **Measure as** | **Count** (counts total events) |
   | **Event Type** | `user.login` |



## Dashboard Engagement

   | Field | Value |
   |-------|-------|
   | **Name** | <pre>`Dashboard Engagement`</pre> |
   | **Owners** | All Project Users |
   | **Traffic Type** | `user` |
   | **Measure as** | **Count** (counts total events) |
   | **Event Type** | `feature.dashboard_viewed` |

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
   | **Name** | <pre>`Login Success Guardrail`</pre> |
   | **Metric category** | Guardrail metrics |
   | **Traffic Type** | `user` |
   | **Measure as** | **Sum** (sum of event values) |
   | **Event Type** | `user.login` |

3. Click **Create**
4. From the Navigation bar select **Alert Policy**
5. Click **Create alert policy**

   | Field | Value |
   |-------|-------|
   | **Name** | <pre>`reduced footprint`</pre>|
   | **Choose your environment** | stg-.... |
   | **Alert degradation** | `-20%` (alert if metric drops more than 20%) |

7. Click **Create alert policy**.

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

1. Go to **Feature Flags** → Select `target_country` 
2. Navigate to the **Metrics Impact** tab.
3. Click **Add Metrics**.
4. Select:
   - ✅ `Feature Evaluation Success` (primary metric)
   - ✅ `User Login Rate` (guardrail)
   - ✅ `Dashboard Engagement` (secondary metric)
5. Click **Save**.
6. Click **"Recalculate All Metrics"** to ensure all metric calculations are up to date.

> [!WARNING]
> Note that the guardrail metric is added automatically to reduce mean time to detect any performance issue

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

#### Audit events

1. From the left hand side menu navigate to **Data Hub**
2. Select the stg environment
3. From the nav bar switch to **Live tail**
4. Change data type to **Events**
5. Start the audit by clicking **Query**
6. Confirm you see:
   - `feature.evaluated`
   - `user.login`
   - `feature.dashboard_viewed`

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

## 🎓 8. Review and Wrap-Up

### ✅ You’ve completed the Feature Management & Experimentation Workshop!

You’ve now explored:
- Environment setup and SDK key configuration  
- Creating and targeting feature flags  
- Attribute-based and segment-based targeting  
- Progressive rollouts and percentage-based rollouts
- Static and dynamic segments
- **Experimentation with metrics and guardrails** ✨
- Automated and scheduled traffic simulation

🎉 **Great job!**


