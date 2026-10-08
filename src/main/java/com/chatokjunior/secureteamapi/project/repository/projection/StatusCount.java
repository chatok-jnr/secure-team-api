package com.chatokjunior.secureteamapi.project.repository.projection;

import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;

public interface StatusCount {
    ProjectStatus getStatus();
    Long getCount();
}
