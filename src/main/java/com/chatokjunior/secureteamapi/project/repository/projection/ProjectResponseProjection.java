package com.chatokjunior.secureteamapi.project.repository.projection;

import java.util.UUID;

public record ProjectResponseProjection(
        UUID id,
        String name,
        String description
) {
}
