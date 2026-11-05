package com.chatokjunior.secureteamapi.user.dto;

import com.chatokjunior.secureteamapi.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoleUpdateRequest {
    @NotNull
    private Role role;
}
