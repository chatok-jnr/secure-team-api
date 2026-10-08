package com.chatokjunior.secureteamapi.project.dto;

import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProjectUpdateRequest(
        @Size(min = 2, max = 25)
        String name,
        @Size(min = 2, max = 700)
        String description,
        ProjectStatus status,
        UUID managerId
) {
}
