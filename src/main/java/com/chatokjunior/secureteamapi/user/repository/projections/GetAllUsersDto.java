package com.chatokjunior.secureteamapi.user.repository.projections;

import com.chatokjunior.secureteamapi.user.entity.Role;

import java.util.UUID;

public record GetAllUsersDto(UUID id, String email, String fullName, Role role) {
}
