package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Feedback repository for database operations
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    // Find feedback by goal
    List<Feedback> findByGoal_GoalId(Integer goalId);
    
    // Find feedback by review
    List<Feedback> findByReview_ReviewId(Integer reviewId);
}
