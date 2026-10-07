package com.chatokjunior.secureteamapi.project.repository.projection;

import com.chatokjunior.secureteamapi.user.entity.Role;

import java.util.UUID;

public record ProjectMemberProjection(
        UUID id,
        String fullName,
        String email,
        Role role,
        boolean enabled
) {
}
