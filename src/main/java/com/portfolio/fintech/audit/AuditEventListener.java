package com.portfolio.fintech.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.fintech.events.AuditEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditEventListener {
    private final AuditLogRepository auditLogs;
    private final ObjectMapper mapper;

    public AuditEventListener(AuditLogRepository auditLogs, ObjectMapper mapper) { this.auditLogs = auditLogs; this.mapper = mapper; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAudit(AuditEvent event) throws Exception {
        auditLogs.save(new AuditLog(event.actor(), event.action(), event.entityType(), event.entityId(), mapper.writeValueAsString(event.metadata())));
    }
}
