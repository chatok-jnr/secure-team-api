package com.chatokjunior.secureteamapi.user.repository.projections;

import com.chatokjunior.secureteamapi.user.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record GetUserDetailsDto(
        UUID id, String fullName, String email,
        Role role, boolean enabled, boolean accountNonLocked,
        Instant createdAt, Instant updatedAt) {
}
