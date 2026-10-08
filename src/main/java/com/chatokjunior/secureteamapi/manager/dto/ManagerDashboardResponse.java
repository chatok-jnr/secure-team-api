package com.chatokjunior.secureteamapi.manager.dto;

import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ManagerDashboardResponse(
        ManagerInfo manager,
        Summary summary,
        List<RecentProject> recentProjects
) {
    public record ManagerInfo(
            UUID id,
            String fullName,
            String email
    ) {}

    public record Summary(
            long totalProjects,
            Map<ProjectStatus, Long> projectsByStatus,
            long totalMembers
    ) {}

    public record RecentProject(
       UUID id,
       String name,
       ProjectStatus status,
       long memberCount
    ) {}
}
