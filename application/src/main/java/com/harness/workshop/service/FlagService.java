package com.harness.workshop.service;

import com.harness.workshop.model.TreatmentView;
import com.harness.workshop.model.User;
import io.split.client.SplitClient;
import io.split.client.SplitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Feature flag evaluations. Impressions are recorded only by {@link SplitClient#getTreatment}
 * (and variants) — never by {@code track()}.
 */
@Service
public class FlagService {

    private static final Logger log = LoggerFactory.getLogger(FlagService.class);

    private final Map<String, User> users = new LinkedHashMap<>();
    private final SplitClient splitClient;
    private final SplitManager splitManager;
    private final FmeEventService eventService;
    private final List<String> configuredSplits;

    public FlagService(
            SplitClient splitClient,
            SplitManager splitManager,
            FmeEventService eventService,
            @Value("${app.splits:}") List<String> splits) {
        this.splitClient = splitClient;
        this.splitManager = splitManager;
        this.eventService = eventService;
        this.configuredSplits = (splits == null) ? List.of() : splits;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void putUser(User u) {
        users.put(u.getId(), u);
    }

    public Optional<User> findUser(String id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<String> listSplitNames() {
        if (configuredSplits != null && !configuredSplits.isEmpty()) {
            return configuredSplits;
        }
        return new ArrayList<>(splitManager.splitNames());
    }

    /**
     * Registers an impression via {@code getTreatment(key, splitName, attributes)} only.
     * Does not call {@code track()} — use before delayed simulation events.
     */
    public TreatmentView evalImpressionOnly(String splitName, User user) {
        String key = user.getId();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("User id (traffic key) is required for getTreatment");
        }
        if (splitName == null || splitName.isBlank()) {
            throw new IllegalArgumentException("Feature flag name is required for getTreatment");
        }

        Map<String, Object> attributes = userAttributes(user);
        String treatment = splitClient.getTreatment(key, splitName, attributes);

        if (treatment == null || "control".equalsIgnoreCase(treatment)) {
            log.warn(
                    "getTreatment returned '{}' for flag='{}' key='{}' — no impression attributed to on/off. "
                            + "Check SPLIT_SDK_KEY, flag initiated in staging, and SDK blockUntilReady.",
                    treatment, splitName, key);
        } else {
            log.debug("Impression registered: flag={} key={} treatment={}", splitName, key, treatment);
        }

        return new TreatmentView(splitName, treatment, null, null);
    }

    /** UI/API: getTreatment (impression) + immediate feature.evaluated event. */
    public TreatmentView eval(String splitName, User user) {
        TreatmentView view = evalImpressionOnly(splitName, user);
        eventService.trackFeatureEvaluated(user, splitName, view.getTreatment());
        return view;
    }

    public Map<String, TreatmentView> evalAllImpressionsOnly(User user) {
        LinkedHashMap<String, TreatmentView> out = new LinkedHashMap<>();
        for (String splitName : listSplitNames()) {
            out.put(splitName, evalImpressionOnly(splitName, user));
        }
        return out;
    }

    public Map<String, TreatmentView> evalAll(User user) {
        LinkedHashMap<String, TreatmentView> out = new LinkedHashMap<>();
        for (String splitName : listSplitNames()) {
            out.put(splitName, eval(splitName, user));
        }
        return out;
    }

    private static Map<String, Object> userAttributes(User user) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("plan", user.getPlan());
        attrs.put("country", user.getCountry());
        if (user.getAttributes() != null) {
            attrs.putAll(user.getAttributes());
        }
        return attrs;
    }
}
