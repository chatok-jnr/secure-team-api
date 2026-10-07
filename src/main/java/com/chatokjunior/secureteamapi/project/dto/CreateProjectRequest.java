package com.chatokjunior.secureteamapi.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank
        @Size(min = 2, max = 25)
        String name,

        @Size(min = 2, max = 700)
        String description
) {
}
