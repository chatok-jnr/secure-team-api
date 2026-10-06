package com.chatokjunior.secureteamapi.user.dto;

import com.chatokjunior.secureteamapi.user.entity.Role;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class RoleUpdateRequest {
    @NonNull
    private Role role;
}
