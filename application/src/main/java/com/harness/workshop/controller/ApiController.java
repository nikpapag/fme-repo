package com.harness.workshop.controller;

import com.harness.workshop.model.EventTrackRequest;
import com.harness.workshop.model.User;
import com.harness.workshop.model.SimulationProgress;
import com.harness.workshop.model.SimulationResult;
import com.harness.workshop.service.FmeEventService;
import com.harness.workshop.service.FlagService;
import com.harness.workshop.service.TrafficSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final FlagService service;
    private final FmeEventService eventService;
    private final TrafficSimulationService simulationService;

    public ApiController(
            FlagService service,
            FmeEventService eventService,
            TrafficSimulationService simulationService) {
        this.service = service;
        this.eventService = eventService;
        this.simulationService = simulationService;
    }

    @GetMapping("/users")
    public Map<String, ?> users() {
        return service.getUsers();
    }

    @GetMapping("/splits")
    public Object splits() {
        return service.listSplitNames();
    }

    @GetMapping("/evaluate/{userId}")
    public ResponseEntity<?> evalAll(@PathVariable String userId) {
        return service.findUser(userId)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(service.evalAll(u)))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "USER_NOT_FOUND")));
    }

    @PostMapping("/evaluate/{split}/{userId}")
    public ResponseEntity<?> evalOne(@PathVariable String split, @PathVariable String userId) {
        return service.findUser(userId)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(service.eval(split, u)))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "USER_NOT_FOUND")));
    }

    /**
     * Track a custom FME event for experimentation metrics.
     * POST /api/events/{userId} with body: { "eventType": "checkout.completed", "value": 1.0, "properties": {} }
     */
    @PostMapping("/events/{userId}")
    public ResponseEntity<?> trackEvent(@PathVariable String userId, @RequestBody EventTrackRequest request) {
        if (request.getEventType() == null || request.getEventType().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "EVENT_TYPE_REQUIRED"));
        }
        return service.findUser(userId)
                .<ResponseEntity<?>>map(u -> {
                    boolean queued = eventService.track(
                            u.getId(),
                            request.getEventType(),
                            request.getValue(),
                            request.getProperties());
                    return ResponseEntity.ok(Map.of(
                            "queued", queued,
                            "userId", u.getId(),
                            "eventType", request.getEventType()));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "USER_NOT_FOUND")));
    }

    /**
     * Generate synthetic impressions and events for experimentation metrics.
     * Use ?blocking=true to wait until complete (used by Selenium tests).
     */
    @PostMapping("/simulate")
    public ResponseEntity<SimulationResult> simulate(
            @RequestParam(defaultValue = "350") int minPerTreatment,
            @RequestParam(defaultValue = "false") boolean blocking) {
        if (blocking) {
            if (simulationService.isRunning()) {
                return ResponseEntity.status(409).body(SimulationResult.alreadyRunning());
            }
            return ResponseEntity.ok(simulationService.runSimulationBlocking(minPerTreatment));
        }
        return ResponseEntity.accepted().body(simulationService.startSimulation(minPerTreatment));
    }

    @GetMapping("/simulate/status")
    public SimulationProgress simulateStatus() {
        return simulationService.getProgress();
    }

    /**
     * Fires a single getTreatment per flag for a user — use to verify impressions (not track).
     */
    @GetMapping("/diagnostics/impressions")
    public ResponseEntity<?> diagnosticsImpressions(@RequestParam(defaultValue = "u001") String userId) {
        return service.findUser(userId)
                .<ResponseEntity<?>>map(u -> {
                    var flags = service.listSplitNames();
                    if (flags.isEmpty()) {
                        return ResponseEntity.ok(Map.of(
                                "error", "NO_FLAGS",
                                "hint", "Check SPLIT_SDK_KEY and initiate flags in Harness FME staging"));
                    }
                    Map<String, Object> evaluations = new LinkedHashMap<>();
                    for (String flag : flags) {
                        var view = service.evalImpressionOnly(flag, u);
                        evaluations.put(flag, Map.of(
                                "treatment", view.getTreatment(),
                                "registersImpression", !"control".equalsIgnoreCase(
                                        view.getTreatment() == null ? "control" : view.getTreatment())));
                    }
                    return ResponseEntity.ok(Map.of(
                            "userId", userId,
                            "trafficKey", u.getId(),
                            "note", "Impressions only come from getTreatment, not track",
                            "evaluations", evaluations));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "USER_NOT_FOUND")));
    }
}
