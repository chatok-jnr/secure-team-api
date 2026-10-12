package com.chatokjunior.secureteamapi.audit.dto;

import com.chatokjunior.secureteamapi.audit.entity.AuditAction;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        AuditAction auditAction,
        String actorEmail,
        String targetType,
        String targetId,
        String details,
        String ipAddress,
        String userAgent,
        Instant createdAt
) {
}
