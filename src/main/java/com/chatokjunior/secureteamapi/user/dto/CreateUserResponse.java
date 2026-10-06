package com.chatokjunior.secureteamapi.user.dto;

import com.chatokjunior.secureteamapi.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class CreateUserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
    private boolean accountNonLocked;
    private Instant createdAt;
}
