package com.knowu.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knowu.model.Habit;
import com.knowu.model.HabitLog;
import com.knowu.model.User;
import com.knowu.repository.HabitLogRepository;
import com.knowu.repository.HabitRepository;
import com.knowu.repository.UserRepository;

@Service
public class HabitService {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    @Autowired
    private UserRepository userRepository;

    public Habit createHabit(Habit habit) {
        return habitRepository.save(habit);
    }

    public List<Habit> getUserHabits(Long userId) {
        return habitRepository.findByUserIdAndActiveTrue(userId);
    }

    @Transactional
    public HabitLog logHabitCompletion(Long habitId, User user, String notes) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found!"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to modify this habit!");
        }

        LocalDate today = LocalDate.now();
        Optional<HabitLog> existingLog = habitLogRepository.findByHabitIdAndCompletedDate(habitId, today);

        if (existingLog.isPresent()) {
            throw new RuntimeException("Habit already logged for today!");
        }

        // Create log
        HabitLog log = new HabitLog(
                habit,
                today,
                notes,
                LocalDateTime.now()
        );

        habitLogRepository.save(log);

        // Update gamification points for the user
        user.setXp(user.getXp() + 10);

        // Level up if XP exceeds a threshold (e.g., 100 XP per level)
        if (user.getXp() >= user.getLevel() * 100) {
            user.setLevel(user.getLevel() + 1);
        }
        userRepository.save(user);

        // Update habit streak simplified (just increment for today)
        // A true streak requires checking yesterday's log, but this is a simplified MVP
        // calculation.
        habit.setCurrentStreak(habit.getCurrentStreak() + 1);
        habitRepository.save(habit);

        return log;
    }

    public List<HabitLog> getHeatmapData(Long userId, LocalDate startDate, LocalDate endDate) {
        return habitLogRepository.findByHabitUserIdAndCompletedDateBetween(userId, startDate, endDate);
    }

    @Transactional
    public Habit updateHabit(Long habitId, User user, String title, String description, String frequencyType) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found!"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to modify this habit!");
        }

        habit.setTitle(title);
        habit.setDescription(description);
        habit.setFrequencyType(frequencyType);

        return habitRepository.save(habit);
    }

    @Transactional
    public void deleteHabit(Long habitId, User user) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found!"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to modify this habit!");
        }

        // Delete associated logs first to avoid foreign key constraints
        List<HabitLog> logs = habitLogRepository.findByHabitId(habitId);
        habitLogRepository.deleteAll(logs);

        habitRepository.delete(habit);
    }
}
