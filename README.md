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
1. From the **left-hand side menu**, navigate to **FME Settings**.  
2. Among all visible projects, click **View** for the one relevant to your user.  
3. You should see two environments — by default, we’ll use the **staging** environment for this lab.

<img width="783" height="191" alt="image" src="https://github.com/user-attachments/assets/097c1f2b-6815-41e0-ab39-28bc0851057d" />

---

### Step 2: Obtain SDK Key
1. ####### From the **top navigation bar**, select **SDK API Keys**.
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

## 🧠 8. Review and Wrap-Up

1. Switch to the **Synchronizer Demo** with the workshop coordinator.  
2. Review **configuration/code changes**.  
3. Discuss your results and insights.

---

### ✅ You’ve completed the Feature Management & Experimentation Workshop!

You’ve now explored:
- Environment setup and SDK key configuration  
- Creating and targeting feature flags  
- Attribute-based and segment-based targeting  
- Progressive rollouts  
- Chaos testing and SDK resilience

🎉 **Great job!**
