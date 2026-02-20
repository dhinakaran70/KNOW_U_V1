package com.knowu.repository;

import com.knowu.model.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByHabitId(Long habitId);

    // Find all logs for a user within a date range (for heatmap)
    List<HabitLog> findByHabitUserIdAndCompletedDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<HabitLog> findByHabitIdAndCompletedDate(Long habitId, LocalDate completedDate);
}
