package com.portfolio.fintech.events;

import java.util.Map;

public record AuditEvent(String actor, String action, String entityType, String entityId, Map<String, Object> metadata) {}
