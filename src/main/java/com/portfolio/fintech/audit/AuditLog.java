package com.portfolio.fintech.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = @Index(name = "idx_audit_entity", columnList = "entityType,entityId"))
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String actor;
    @Column(nullable = false, length = 80)
    private String action;
    @Column(nullable = false, length = 80)
    private String entityType;
    @Column(nullable = false, length = 120)
    private String entityId;
    @Column(nullable = false, length = 2000)
    private String metadata;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {}
    public AuditLog(String actor, String action, String entityType, String entityId, String metadata) {
        this.actor = actor; this.action = action; this.entityType = entityType; this.entityId = entityId; this.metadata = metadata;
    }
}
