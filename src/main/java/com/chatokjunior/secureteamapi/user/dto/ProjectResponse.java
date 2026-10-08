package com.chatokjunior.secureteamapi.user.dto;

import com.chatokjunior.secureteamapi.project.entity.Project;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UUID managerId
) {
}
