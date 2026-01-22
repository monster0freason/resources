package com.project.performanceTrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Audit log entity - tracks all system actions
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer auditId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false, length = 100)
    private String action;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    @Column(name = "related_entity_id")
    private Integer relatedEntityId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(length = 20)
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
