package com.knowu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "habits")
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String frequencyType; // DAILY, WEEKLY, SPECIFIC_DAYS

    // Comma-separated list like "MONDAY,WEDNESDAY" if frequencyType is
    // SPECIFIC_DAYS
    private String targetDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean active = true;

    private int currentStreak = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Habit() {
    }

    public Habit(String title, String description, String frequencyType, String targetDays, User user, boolean active, int currentStreak) {
        this.title = title;
        this.description = description;
        this.frequencyType = frequencyType;
        this.targetDays = targetDays;
        this.user = user;
        this.active = active;
        this.currentStreak = currentStreak;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public String getTargetDays() {
        return targetDays;
    }

    public void setTargetDays(String targetDays) {
        this.targetDays = targetDays;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
