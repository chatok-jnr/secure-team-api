package com.chatokjunior.secureteamapi.project.dto;

import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CreateProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID managerId;
    private ProjectStatus status;
    private Instant createdAt;
}
