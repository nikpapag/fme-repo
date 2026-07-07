package com.harness.workshop.service;

import com.harness.workshop.model.SimulationProgress;
import com.harness.workshop.model.SimulationResult;
import com.harness.workshop.model.TreatmentView;
import com.harness.workshop.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates synthetic traffic using a strict impression → delay → event sequence per metric:
 * <ol>
 *   <li>{@code getTreatment} for all flags (registers impressions)</li>
 *   <li>Wait {@code simulation.eventDelayMs} so events associate with those impressions</li>
 *   <li>Send the metric's {@code track} event(s) with treatment-skewed values (on = positive, off = negative)</li>
 * </ol>
 * Steps 1–3 repeat for each event type in order.
 */
@Service
public class TrafficSimulationService {

    private static final Logger log = LoggerFactory.getLogger(TrafficSimulationService.class);

    private static final String[] PLANS = {"free", "pro", "enterprise"};
    private static final String[] COUNTRIES = {"UK", "US", "IN", "BR", "DE", "AE", "JP"};
    private static final Set<String> TARGET_TREATMENTS = Set.of("on", "off");

    /** Ordered metric steps — each runs impressions then delayed events. */
    private static final List<String> METRIC_STEPS_IN_ORDER = List.of(
            FmeEventService.EVENT_FEATURE_EVALUATED,
            FmeEventService.EVENT_USER_LOGIN,
            FmeEventService.EVENT_FEATURE_DASHBOARD_VIEWED,
            FmeEventService.EVENT_USER_IMPERSONATED
    );

    private final FlagService flagService;
    private final FmeEventService eventService;
    private final int defaultMinPerTreatment;
    private final int maxSyntheticUsers;
    private final int eventDelayMs;
    private final int eventFlushIntervalMs;
    private final int impressionsRefreshRateSec;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger syntheticUserCount = new AtomicInteger();
    private final AtomicInteger totalImpressions = new AtomicInteger();
    private final AtomicInteger totalEvents = new AtomicInteger();
    private volatile String progressMessage = "Idle";
    private volatile SimulationResult lastResult;

    private final Map<String, Map<String, AtomicInteger>> impressionCounts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Map<String, AtomicInteger>>> eventCounts = new ConcurrentHashMap<>();

    public TrafficSimulationService(
            FlagService flagService,
            FmeEventService eventService,
            @Value("${simulation.minPerTreatment:350}") int defaultMinPerTreatment,
            @Value("${simulation.maxSyntheticUsers:25000}") int maxSyntheticUsers,
            @Value("${simulation.eventDelayMs:3000}") int eventDelayMs,
            @Value("${split.eventFlushIntervalMs:10000}") int eventFlushIntervalMs,
            @Value("${split.impressionsRefreshRate:5}") int impressionsRefreshRateSec) {
        this.flagService = flagService;
        this.eventService = eventService;
        this.defaultMinPerTreatment = defaultMinPerTreatment;
        this.maxSyntheticUsers = maxSyntheticUsers;
        this.eventDelayMs = eventDelayMs;
        this.eventFlushIntervalMs = eventFlushIntervalMs;
        this.impressionsRefreshRateSec = impressionsRefreshRateSec;
    }

    public SimulationProgress getProgress() {
        return new SimulationProgress(
                running.get(),
                syntheticUserCount.get(),
                defaultMinPerTreatment,
                totalImpressions.get(),
                totalEvents.get(),
                !running.get() && lastResult != null && "COMPLETE".equals(lastResult.getStatus()),
                progressMessage,
                snapshotImpressions(),
                lastResult);
    }

    public boolean isRunning() {
        return running.get();
    }

    public SimulationResult startSimulation() {
        return startSimulation(defaultMinPerTreatment);
    }

    public SimulationResult startSimulation(int minPerTreatment) {
        if (!running.compareAndSet(false, true)) {
            return SimulationResult.alreadyRunning();
        }
        resetCounters();
        CompletableFuture.runAsync(() -> {
            try {
                lastResult = executeSimulation(minPerTreatment);
            } catch (Exception e) {
                log.error("Simulation failed", e);
                lastResult = failedResult(minPerTreatment, e.getMessage());
            } finally {
                running.set(false);
            }
        });
        return new SimulationResult(
                "STARTED",
                "Simulation started in background",
                0, 0, 0, minPerTreatment, 0, Map.of(), Map.of());
    }

    public SimulationResult runSimulationBlocking() {
        return runSimulationBlocking(defaultMinPerTreatment);
    }

    public SimulationResult runSimulationBlocking(int minPerTreatment) {
        if (!running.compareAndSet(false, true)) {
            return SimulationResult.alreadyRunning();
        }
        try {
            resetCounters();
            lastResult = executeSimulation(minPerTreatment);
            return lastResult;
        } catch (Exception e) {
            log.error("Simulation failed", e);
            lastResult = failedResult(minPerTreatment, e.getMessage());
            return lastResult;
        } finally {
            running.set(false);
        }
    }

    private SimulationResult failedResult(int minPerTreatment, String message) {
        return new SimulationResult(
                "FAILED",
                message,
                syntheticUserCount.get(),
                totalImpressions.get(),
                totalEvents.get(),
                minPerTreatment,
                0,
                snapshotImpressions(),
                snapshotEvents());
    }

    private SimulationResult executeSimulation(int minPerTreatment) {
        long start = System.currentTimeMillis();
        List<String> splits = flagService.listSplitNames();
        if (splits.isEmpty()) {
            return SimulationResult.noFlags();
        }

        initCountMaps(splits);
        progressMessage = "Starting simulation (impressions → delay → events per metric)…";
        Random random = new Random();
        User previousUser = null;

        for (int i = 0; i < maxSyntheticUsers && !targetsMet(splits, minPerTreatment); i++) {
            User user = randomSyntheticUser(i, random);

            for (String eventType : METRIC_STEPS_IN_ORDER) {
                if (FmeEventService.EVENT_USER_IMPERSONATED.equals(eventType)) {
                    if (previousUser == null || i % 40 != 0) {
                        continue;
                    }
                    runMetricCycle(user, previousUser, splits, eventType);
                } else {
                    runMetricCycle(user, null, splits, eventType);
                }
            }

            previousUser = user;
            syntheticUserCount.incrementAndGet();

            if (i % 25 == 0) {
                progressMessage = String.format(
                        "User %d — %d impressions, %d events (target ≥%d per on/off per metric)",
                        i + 1, totalImpressions.get(), totalEvents.get(), minPerTreatment);
            }
        }

        flushImpressionsAndEvents();

        boolean met = targetsMet(splits, minPerTreatment);
        long duration = System.currentTimeMillis() - start;
        String message = met
                ? String.format(
                "Complete: impressions first, then events (%dms delay). "
                        + "On=treatment-positive values, off=negative/baseline. ≥%d per on/off per metric.",
                eventDelayMs, minPerTreatment)
                : String.format(
                "Stopped after %d users — not all targets met (use on/off rollouts, not 100%% single treatment)",
                syntheticUserCount.get());

        progressMessage = message;
        return new SimulationResult(
                met ? "COMPLETE" : "PARTIAL",
                message,
                syntheticUserCount.get(),
                totalImpressions.get(),
                totalEvents.get(),
                minPerTreatment,
                duration,
                snapshotImpressions(),
                snapshotEvents());
    }

    /**
     * One metric cycle: (1) getTreatment impressions, (2) delay, (3) track events.
     *
     * @param impressionUser user key used for getTreatment (must match subsequent track key)
     * @param impersonationActor if non-null, sends user.impersonated after delay (actor → impressionUser)
     */
    private void runMetricCycle(
            User impressionUser,
            User impersonationActor,
            List<String> splits,
            String eventType) {

        progressMessage = String.format(
                "Step: impressions for [%s] (user %s)…",
                eventType, impressionUser.getId());

        Map<String, String> treatmentsBySplit = registerImpressions(impressionUser, splits);

        waitAfterImpressions(eventType);

        progressMessage = String.format(
                "Step: events for [%s] after %dms delay (user %s)…",
                eventType, eventDelayMs, impressionUser.getId());

        dispatchEventsForMetric(impressionUser, impersonationActor, splits, eventType, treatmentsBySplit);
    }

    /** Step 1 — impressions only via getTreatment. */
    private Map<String, String> registerImpressions(User user, List<String> splits) {
        Map<String, String> treatmentsBySplit = new LinkedHashMap<>();
        for (String split : splits) {
            TreatmentView view = flagService.evalImpressionOnly(split, user);
            String treatment = normalizeTreatment(view.getTreatment());
            treatmentsBySplit.put(split, treatment);
            incrementImpression(split, treatment);
        }
        return treatmentsBySplit;
    }

    /** Step 2 — delay so Harness FME can associate events with prior impressions. */
    private void waitAfterImpressions(String eventType) {
        try {
            log.debug("Waiting {}ms after impressions before [{}]", eventDelayMs, eventType);
            Thread.sleep(eventDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulation interrupted during event delay", e);
        }
    }

    /** Step 3 — track events with positive (on) or negative (off) metric values. */
    private void dispatchEventsForMetric(
            User impressionUser,
            User impersonationActor,
            List<String> splits,
            String eventType,
            Map<String, String> treatmentsBySplit) {

        switch (eventType) {
            case FmeEventService.EVENT_FEATURE_EVALUATED -> {
                for (String split : splits) {
                    String treatment = treatmentsBySplit.get(split);
                    eventService.trackSimulationEvent(
                            impressionUser, eventType, split, treatment, treatmentsBySplit);
                    incrementEvent(eventType, split, treatment);
                }
            }
            case FmeEventService.EVENT_USER_LOGIN -> {
                eventService.trackUserLogin(impressionUser, treatmentsBySplit);
                attributeEventToTreatments(eventType, treatmentsBySplit);
            }
            case FmeEventService.EVENT_FEATURE_DASHBOARD_VIEWED -> {
                eventService.trackDashboardViewed(impressionUser, treatmentsBySplit, splits.size());
                attributeEventToTreatments(eventType, treatmentsBySplit);
            }
            case FmeEventService.EVENT_USER_IMPERSONATED -> {
                if (impersonationActor != null) {
                    eventService.trackUserImpersonated(impersonationActor, impressionUser, treatmentsBySplit);
                    attributeEventToTreatments(eventType, treatmentsBySplit);
                }
            }
            default -> log.warn("Unknown simulation event type: {}", eventType);
        }
    }

    private void resetCounters() {
        syntheticUserCount.set(0);
        totalImpressions.set(0);
        totalEvents.set(0);
        impressionCounts.clear();
        eventCounts.clear();
        progressMessage = "Starting…";
    }

    private void initCountMaps(List<String> splits) {
        for (String split : splits) {
            impressionCounts.put(split, new ConcurrentHashMap<>());
            for (String eventType : METRIC_STEPS_IN_ORDER) {
                eventCounts
                        .computeIfAbsent(eventType, k -> new ConcurrentHashMap<>())
                        .put(split, new ConcurrentHashMap<>());
            }
        }
    }

    private User randomSyntheticUser(int index, Random random) {
        String id = "sim-" + UUID.randomUUID().toString().substring(0, 12);
        String plan = PLANS[random.nextInt(PLANS.length)];
        String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
        Map<String, String> attrs = new HashMap<>();
        attrs.put("beta", random.nextBoolean() ? "true" : "false");
        attrs.put("simulated", "true");
        attrs.put("batch", String.valueOf(index / 100));
        return new User(id, "SimUser-" + index, id + "@sim.local", plan, country, attrs);
    }

    private String normalizeTreatment(String treatment) {
        if (treatment == null) {
            return "control";
        }
        return treatment.toLowerCase(Locale.ROOT);
    }

    private void incrementImpression(String split, String treatment) {
        impressionCounts
                .computeIfAbsent(split, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(treatment, t -> new AtomicInteger())
                .incrementAndGet();
        totalImpressions.incrementAndGet();
    }

    private void incrementEvent(String eventType, String split, String treatment) {
        eventCounts
                .computeIfAbsent(eventType, e -> new ConcurrentHashMap<>())
                .computeIfAbsent(split, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(treatment, t -> new AtomicInteger())
                .incrementAndGet();
        totalEvents.incrementAndGet();
    }

    private void attributeEventToTreatments(String eventType, Map<String, String> treatmentsBySplit) {
        for (Map.Entry<String, String> entry : treatmentsBySplit.entrySet()) {
            incrementEvent(eventType, entry.getKey(), entry.getValue());
        }
    }

    private boolean targetsMet(List<String> splits, int minPerTreatment) {
        for (String split : splits) {
            for (String treatment : TARGET_TREATMENTS) {
                if (count(impressionCounts, split, treatment) < minPerTreatment) {
                    return false;
                }
                for (String eventType : METRIC_STEPS_IN_ORDER) {
                    if (count(eventCounts, eventType, split, treatment) < minPerTreatment) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static int count(Map<String, Map<String, AtomicInteger>> map, String split, String treatment) {
        return map.getOrDefault(split, Map.of()).getOrDefault(treatment, new AtomicInteger()).get();
    }

    private static int count(
            Map<String, Map<String, Map<String, AtomicInteger>>> map,
            String eventType,
            String split,
            String treatment) {
        return map.getOrDefault(eventType, Map.of())
                .getOrDefault(split, Map.of())
                .getOrDefault(treatment, new AtomicInteger())
                .get();
    }

    private Map<String, Map<String, Integer>> snapshotImpressions() {
        Map<String, Map<String, Integer>> out = new LinkedHashMap<>();
        impressionCounts.forEach((split, treatments) -> {
            Map<String, Integer> t = new LinkedHashMap<>();
            treatments.forEach((tr, c) -> t.put(tr, c.get()));
            out.put(split, t);
        });
        return out;
    }

    private Map<String, Map<String, Map<String, Integer>>> snapshotEvents() {
        Map<String, Map<String, Map<String, Integer>>> out = new LinkedHashMap<>();
        eventCounts.forEach((eventType, splits) -> {
            Map<String, Map<String, Integer>> perSplit = new LinkedHashMap<>();
            splits.forEach((split, treatments) -> {
                Map<String, Integer> t = new LinkedHashMap<>();
                treatments.forEach((tr, c) -> t.put(tr, c.get()));
                perSplit.put(split, t);
            });
            out.put(eventType, perSplit);
        });
        return out;
    }

    /**
     * Impressions (getTreatment) and events (track) flush on separate SDK timers.
     * Wait long enough for both pipelines to reach Harness FME.
     */
    private void flushImpressionsAndEvents() {
        long impressionWaitMs = (impressionsRefreshRateSec * 1000L) + 3000L;
        long eventWaitMs = eventFlushIntervalMs + 2000L;
        long totalWait = Math.max(impressionWaitMs, eventWaitMs);
        progressMessage = String.format(
                "Flushing impressions (~%ds) and events (~%ds) to Harness FME…",
                impressionsRefreshRateSec, eventFlushIntervalMs / 1000);
        try {
            Thread.sleep(totalWait);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
