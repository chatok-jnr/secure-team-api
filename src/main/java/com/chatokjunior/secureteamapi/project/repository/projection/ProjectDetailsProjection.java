package com.chatokjunior.secureteamapi.project.repository.projection;

import java.time.Instant;
import java.util.UUID;

public record ProjectDetailsProjection(
        UUID id,
        String name,
        String description,
        UUID managerId,
        String managerName,
        String managerEmail,
        Instant createdAt,
        Instant updatedAt
) {
}
