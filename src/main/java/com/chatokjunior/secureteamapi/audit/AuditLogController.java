package com.chatokjunior.secureteamapi.audit;

import com.chatokjunior.secureteamapi.audit.dto.AuditLogResponse;
import com.chatokjunior.secureteamapi.audit.entity.AuditAction;
import com.chatokjunior.secureteamapi.audit.services.AuditLogQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin/audit-logs")
@RestController
@RequiredArgsConstructor
@Validated
public class AuditLogController {
    private final AuditLogQueryService auditLogQueryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            AuditAction action,

            @RequestParam(required = false)
            String actorEmail
    ) {
        return ResponseEntity.ok(
                auditLogQueryService.getAuditLogs(
                        page,
                        size,
                        action,
                        actorEmail
                )
        );
    }
}
