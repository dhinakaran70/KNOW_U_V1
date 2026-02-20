package com.knowu.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knowu.model.Habit;
import com.knowu.model.HabitLog;
import com.knowu.model.User;
import com.knowu.payload.request.HabitLogRequest;
import com.knowu.payload.request.HabitRequest;
import com.knowu.payload.response.MessageResponse;
import com.knowu.repository.UserRepository;
import com.knowu.service.HabitService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/habits")
public class HabitController {

    @Autowired
    private HabitService habitService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<?> createHabit(@Valid @RequestBody HabitRequest request) {
        User user = getAuthenticatedUser();

        Habit habit = new Habit(
                request.getTitle(),
                request.getDescription(),
                request.getFrequencyType(),
                request.getTargetDays(),
                user,
                true,
                0
        );

        Habit savedHabit = habitService.createHabit(habit);
        return ResponseEntity.ok(savedHabit);
    }

    @GetMapping
    public ResponseEntity<?> getUserHabits() {
        User user = getAuthenticatedUser();
        List<Habit> habits = habitService.getUserHabits(user.getId());
        return ResponseEntity.ok(habits);
    }

    @PutMapping("/{habitId}")
    public ResponseEntity<?> updateHabit(@PathVariable Long habitId, @Valid @RequestBody HabitRequest request) {
        User user = getAuthenticatedUser();
        try {
            Habit updatedHabit = habitService.updateHabit(
                    habitId, user, request.getTitle(), request.getDescription(), request.getFrequencyType());
            return ResponseEntity.ok(updatedHabit);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<?> deleteHabit(@PathVariable Long habitId) {
        User user = getAuthenticatedUser();
        try {
            habitService.deleteHabit(habitId, user);
            return ResponseEntity.ok(new MessageResponse("Habit deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{habitId}/log")
    public ResponseEntity<?> logHabit(@PathVariable Long habitId,
            @RequestBody(required = false) HabitLogRequest request) {
        User user = getAuthenticatedUser();
        String notes = request != null ? request.getNotes() : null;

        try {
            HabitLog log = habitService.logHabitCompletion(habitId, user, notes);
            return ResponseEntity.ok(log);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/analytics/heatmap")
    public ResponseEntity<?> getHeatmapData(@RequestParam String startDate, @RequestParam String endDate) {
        User user = getAuthenticatedUser();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<HabitLog> logs = habitService.getHeatmapData(user.getId(), start, end);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/analytics/gamification")
    public ResponseEntity<?> getGamificationStats() {
        User user = getAuthenticatedUser();
        Map<String, Object> stats = new HashMap<>();
        stats.put("xp", user.getXp());
        stats.put("level", user.getLevel());
        stats.put("totalHabits", habitService.getUserHabits(user.getId()).size());
        return ResponseEntity.ok(stats);
    }
}
