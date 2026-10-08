package com.chatokjunior.secureteamapi.project.repository.projection;

import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;

import java.util.UUID;

public interface RecentProjectRow {
    UUID getId();
    String getName();
    ProjectStatus getStatus();
    Long getMemberCount();
}
