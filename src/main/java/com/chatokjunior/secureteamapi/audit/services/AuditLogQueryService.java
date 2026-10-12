package com.chatokjunior.secureteamapi.audit.services;

import com.chatokjunior.secureteamapi.audit.AuditLogRepository;
import com.chatokjunior.secureteamapi.audit.dto.AuditLogResponse;
import com.chatokjunior.secureteamapi.audit.entity.AuditAction;
import com.chatokjunior.secureteamapi.audit.entity.AuditLog;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(
            int page,
            int size,
            AuditAction action,
            String actorEmail
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AuditLog> specification =
        (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if(action != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("action"), action)
            );
        }

        if (actorEmail != null && !actorEmail.isBlank()) {
            String emailPattern = "%"
                    + actorEmail.trim().toLowerCase()
                    + "%";

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("actorEmail")),
                                    emailPattern
                            )
            );
        }

        return auditLogRepository.findAll(specification, pageable)
                .map(this::toResponse);
    }

    // ========================
    // Helper Function
    // ========================

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getActorEmail(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getDetails(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getCreatedAt()
        );
    }
}
