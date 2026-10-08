package com.chatokjunior.secureteamapi.project.dto;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UUID managerId
) {
}
