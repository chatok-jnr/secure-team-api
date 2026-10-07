package com.chatokjunior.secureteamapi.project.dto;

import com.chatokjunior.secureteamapi.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class ProjectMemberResponse {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
}
