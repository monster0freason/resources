package com.project.performanceTrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Report Entity
 * -------------
 * Stores the metadata and snapshot data of generated analytics reports.
 * Primarily used by Admins in Phase 11 to track organizational performance trends.
 */
@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId; // Unique identifier for the generated report file

    /**
     * Report Coverage (Workflow Phase: Phase 11, Step 5)
     * Defines the "Who" and "Where" of the report.
     * Values: "Company-wide", "Engineering Department", "Individual: Rahul Sharma".
     */
    @Column(nullable = false, length = 50)
    private String scope;

    /**
     * Data Snapshot (Workflow Phase: Phase 11, Step 1-3)
     * Stores the calculated numbers as a JSON string.
     * Includes metrics like: "Avg Rating: 3.6", "Goal Completion Rate: 52%",
     * and "Evidence Verification Time: 1.8 days".
     * This prevents the need to re-query thousands of goals to see old stats.
     */
    @Column(columnDefinition = "TEXT")
    private String metrics;

    /**
     * Output Type (Workflow Phase: Phase 11, Step 5)
     * E.g., "PDF", "EXCEL", or "CSV".
     * Reflects the user's choice in the Export Dialog before generation.
     */
    @Column(length = 20)
    private String format;

    /**
     * The Creator (Workflow Phase: Phase 11, Step 5)
     * Links to the Admin (ID 500) who triggered the "Generate Report" action.
     * Essential for accountability and the audit trail.
     */
    @ManyToOne
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    /**
     * Creation Timestamp (Workflow Phase: Phase 11, Step 5)
     * Records exactly when the executive summary was finalized.
     * Helps in sorting reports by "Latest" on the Analytics Dashboard.
     */
    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;

    /**
     * File Storage Location (Workflow Phase: Retrieval)
     * The URL or server path where the actual PDF/Excel file is stored.
     * E.g., "/reports/Q1_2026_Executive_Summary.pdf".
     * Allows the Admin to re-download the report without regenerating it.
     */
    @Column(name = "file_path", length = 500)
    private String filePath;
}