package com.harness.workshop.model;


import java.util.Map;
import java.util.function.Predicate;

public class TargetSegment {
    private String key;    // e.g., "beta_testers"
    private String name;   // e.g., "Beta Testers"
    private String description;

    // Simple attribute-matcher: all provided key/value pairs must match.
    private Map<String, String> matchAll;

    public TargetSegment() {}
    public TargetSegment(String key, String name, String description, Map<String, String> matchAll) {
        this.key = key; this.name = name; this.description = description; this.matchAll = matchAll;
    }

    public boolean matches(User user) {
        if (matchAll == null || matchAll.isEmpty()) return false;
        return matchAll.entrySet().stream()
                .allMatch(e -> e.getValue().equalsIgnoreCase(valueFor(user, e.getKey())));
    }

    private String valueFor(User u, String key) {
        if ("plan".equals(key)) {
            return u.getPlan();
        } else if ("country".equals(key)) {
            return u.getCountry();
        } else {
            return u.getAttributes() == null ? null : u.getAttributes().get(key);
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
