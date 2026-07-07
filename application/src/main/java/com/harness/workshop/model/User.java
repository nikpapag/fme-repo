package com.harness.workshop.model;

import java.util.Map;

public class User {
    private String id;         // stable unique id for hashing
    private String name;
    private String email;
    private String plan;       // e.g., "free", "pro", "enterprise"
    private String country;    // e.g., "UK", "US", "IN"
    private Map<String, String> attributes; // arbitrary traits

    public User() {}
    public User(String id, String name, String email, String plan, String country, Map<String,String> attributes) {
        this.id = id; this.name = name; this.email = email; this.plan = plan; this.country = country; this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
