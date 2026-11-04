package com.chatokjunior.secureteamapi.auth.dto;

import com.chatokjunior.secureteamapi.user.entity.Role;
import lombok.*;

import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
}
