package com.harness.workshop.service;

import com.harness.workshop.model.User;
import io.split.client.SplitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Sends Harness FME events via the Java SDK {@link SplitClient#track} method.
 * Event keys must match the traffic type key used in {@code getTreatment} calls
 * so experimentation metrics can attribute events to flag treatments.
 */
@Service
public class FmeEventService {

    private static final Logger log = LoggerFactory.getLogger(FmeEventService.class);

    public static final String EVENT_FEATURE_EVALUATED = "feature.evaluated";
    public static final String EVENT_USER_LOGIN = "user.login";
    public static final String EVENT_USER_IMPERSONATED = "user.impersonated";
    public static final String EVENT_FEATURE_DASHBOARD_VIEWED = "feature.dashboard_viewed";

    /**
     * Metric values when user received {@code on} treatment.
     * Designed to produce varied experiment outcomes:
     * - feature.evaluated: POSITIVE impact (on=1.0 > off=0.1)
     * - user.login: NEGATIVE impact (on=45.0 < off=95.0)
     * - feature.dashboard_viewed: INCONCLUSIVE (on≈off, ~8.2 vs 8.0)
     * - user.impersonated: POSITIVE impact (on=65.0 > off=15.0)
     */
    private static final Map<String, Double> POSITIVE_VALUES = Map.of(
            EVENT_FEATURE_EVALUATED, 1.0,      // POSITIVE: Higher is better
            EVENT_USER_LOGIN, 45.0,             // NEGATIVE: Lower than baseline (showing regression)
            EVENT_FEATURE_DASHBOARD_VIEWED, 8.2, // INCONCLUSIVE: Very close to baseline
            EVENT_USER_IMPERSONATED, 65.0       // POSITIVE: Higher is better
    );

    /**
     * Metric values when user received {@code off} treatment (baseline/control).
     * - feature.evaluated: Low baseline (0.1)
     * - user.login: High baseline (95.0) - treatment causes regression
     * - feature.dashboard_viewed: Similar to on (~8.0) for inconclusive result
     * - user.impersonated: Low baseline (15.0)
     */
    private static final Map<String, Double> NEGATIVE_VALUES = Map.of(
            EVENT_FEATURE_EVALUATED, 0.1,       // Low baseline
            EVENT_USER_LOGIN, 95.0,              // High baseline (on treatment causes drop)
            EVENT_FEATURE_DASHBOARD_VIEWED, 8.0, // Similar to on for inconclusive
            EVENT_USER_IMPERSONATED, 15.0        // Low baseline
    );

    private final SplitClient splitClient;
    private final String trafficType;
    private final boolean eventsEnabled;

    public FmeEventService(
            SplitClient splitClient,
            @Value("${split.trafficType:user}") String trafficType,
            @Value("${split.eventsEnabled:true}") boolean eventsEnabled) {
        this.splitClient = splitClient;
        this.trafficType = trafficType;
        this.eventsEnabled = eventsEnabled;
    }

    public boolean track(String userKey, String eventType) {
        return track(userKey, eventType, null, null);
    }

    public boolean track(String userKey, String eventType, Double value, Map<String, Object> properties) {
        if (!eventsEnabled) {
            return false;
        }
        try {
            boolean queued;
            if (value != null && properties != null && !properties.isEmpty()) {
                queued = splitClient.track(userKey, trafficType, eventType, value, properties);
            } else if (value != null) {
                queued = splitClient.track(userKey, trafficType, eventType, value);
            } else if (properties != null && !properties.isEmpty()) {
                queued = splitClient.track(userKey, trafficType, eventType, properties);
            } else {
                queued = splitClient.track(userKey, trafficType, eventType);
            }
            if (!queued) {
                log.warn("FME event queue full or invalid input: type={} key={}", eventType, userKey);
            }
            return queued;
        } catch (Exception e) {
            log.error("Failed to track FME event {} for key {}", eventType, userKey, e);
            return false;
        }
    }

    /**
     * Tracks a simulation event with a treatment-skewed value so experimentation shows
     * positive lift for {@code on} and negative/baseline impact for {@code off}.
     */
    public boolean trackSimulationEvent(
            User user,
            String eventType,
            String splitName,
            String treatment,
            Map<String, String> allTreatmentsBySplit) {
        boolean onTreatment = isOnTreatment(treatment);
        double value = metricValueForTreatment(eventType, onTreatment);
        Map<String, Object> props = baseUserProperties(user);
        props.put("split", splitName);
        props.put("treatment", normalizeTreatment(treatment));
        props.put("impact", onTreatment ? "positive" : "negative");
        props.put("simulation", true);
        if (allTreatmentsBySplit != null) {
            props.put("treatments", new HashMap<>(allTreatmentsBySplit));
        }
        return track(user.getId(), eventType, value, props);
    }

    public void trackFeatureEvaluated(User user, String splitName, String treatment) {
        trackSimulationEvent(user, EVENT_FEATURE_EVALUATED, splitName, treatment, null);
    }

    /** UI flow — neutral value (not simulation-skewed). */
    public void trackUserLogin(User user) {
        track(user.getId(), EVENT_USER_LOGIN, 1.0, baseUserProperties(user));
    }

    public void trackUserLogin(User user, Map<String, String> treatmentsBySplit) {
        trackForPrimarySplit(user, EVENT_USER_LOGIN, treatmentsBySplit);
    }

    /** UI flow — neutral value (not simulation-skewed). */
    public void trackDashboardViewed(User user, int flagCount) {
        Map<String, Object> props = baseUserProperties(user);
        props.put("flag_count", flagCount);
        track(user.getId(), EVENT_FEATURE_DASHBOARD_VIEWED, (double) flagCount, props);
    }

    public void trackDashboardViewed(User user, Map<String, String> treatmentsBySplit, int flagCount) {
        Map<String, Object> props = baseUserProperties(user);
        props.put("flag_count", flagCount);
        trackWithTreatmentSkew(user, EVENT_FEATURE_DASHBOARD_VIEWED, treatmentsBySplit, props);
    }

    /** UI flow — neutral value (not simulation-skewed). */
    public void trackUserImpersonated(User actor, User target) {
        Map<String, Object> props = baseUserProperties(target);
        if (actor != null) {
            props.put("actor_id", actor.getId());
        }
        track(target.getId(), EVENT_USER_IMPERSONATED, 1.0, props);
    }

    public void trackUserImpersonated(User actor, User target, Map<String, String> treatmentsBySplit) {
        Map<String, Object> props = baseUserProperties(target);
        if (actor != null) {
            props.put("actor_id", actor.getId());
        }
        trackWithTreatmentSkew(target, EVENT_USER_IMPERSONATED, treatmentsBySplit, props);
    }

    private void trackForPrimarySplit(User user, String eventType, Map<String, String> treatmentsBySplit) {
        trackWithTreatmentSkew(user, eventType, treatmentsBySplit, baseUserProperties(user));
    }

    private void trackWithTreatmentSkew(
            User user,
            String eventType,
            Map<String, String> treatmentsBySplit,
            Map<String, Object> extraProps) {
        if (treatmentsBySplit == null || treatmentsBySplit.isEmpty()) {
            track(user.getId(), eventType, NEGATIVE_VALUES.getOrDefault(eventType, 1.0), extraProps);
            return;
        }
        Map.Entry<String, String> primary = treatmentsBySplit.entrySet().iterator().next();
        boolean onTreatment = isOnTreatment(primary.getValue());
        double value = metricValueForTreatment(eventType, onTreatment);
        Map<String, Object> props = new HashMap<>(extraProps);
        props.put("split", primary.getKey());
        props.put("treatment", normalizeTreatment(primary.getValue()));
        props.put("impact", onTreatment ? "positive" : "negative");
        props.put("simulation", true);
        props.put("treatments", new HashMap<>(treatmentsBySplit));
        track(user.getId(), eventType, value, props);
    }

    private static double metricValueForTreatment(String eventType, boolean onTreatment) {
        if (onTreatment) {
            return POSITIVE_VALUES.getOrDefault(eventType, 1.0);
        }
        return NEGATIVE_VALUES.getOrDefault(eventType, 0.1);
    }

    private static boolean isOnTreatment(String treatment) {
        return "on".equalsIgnoreCase(normalizeTreatment(treatment));
    }

    private static String normalizeTreatment(String treatment) {
        return treatment == null ? "control" : treatment.toLowerCase(Locale.ROOT);
    }

    private static Map<String, Object> baseUserProperties(User user) {
        Map<String, Object> props = new HashMap<>();
        props.put("plan", user.getPlan());
        props.put("country", user.getCountry());
        if (user.getAttributes() != null) {
            user.getAttributes().forEach(props::put);
        }
        return props;
    }
}
