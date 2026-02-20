package com.knowu.payload.request;

import jakarta.validation.constraints.NotBlank;

public class HabitRequest {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String frequencyType; // DAILY, WEEKLY, SPECIFIC_DAYS

    private String targetDays; // "MONDAY,WEDNESDAY"

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFrequencyType() { return frequencyType; }
    public void setFrequencyType(String frequencyType) { this.frequencyType = frequencyType; }
    public String getTargetDays() { return targetDays; }
    public void setTargetDays(String targetDays) { this.targetDays = targetDays; }
}
