package com.chatokjunior.secureteamapi.auth.dto;

import com.chatokjunior.secureteamapi.user.entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Setter
@Getter
public class CreateUserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
}
