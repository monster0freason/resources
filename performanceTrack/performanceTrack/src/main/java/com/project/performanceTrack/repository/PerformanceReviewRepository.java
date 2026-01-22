package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.PerformanceReview;
import com.project.performanceTrack.enums.PerformanceReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Performance review repository for database operations
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Integer> {
    
    // Find reviews by user
    List<PerformanceReview> findByUser_UserId(Integer userId);
    
    // Find reviews by cycle
    List<PerformanceReview> findByCycle_CycleId(Integer cycleId);
    
    // Find reviews by status
    List<PerformanceReview> findByStatus(PerformanceReviewStatus status);
    
    // Find review by cycle and user
    Optional<PerformanceReview> findByCycle_CycleIdAndUser_UserId(Integer cycleId, Integer userId);
}
