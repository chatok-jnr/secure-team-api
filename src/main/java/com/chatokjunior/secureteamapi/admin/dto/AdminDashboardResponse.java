package com.chatokjunior.secureteamapi.admin.dto;


import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;
import com.chatokjunior.secureteamapi.user.entity.Role;

import java.util.List;
import java.util.UUID;

public record AdminDashboardResponse(
    Summary summary,
    List<RecentUser> recentUsers,
    List<RecentProject> recentProjects
) {

    public record Summary(
        long totalUsers,
        long totalAdmins,
        long totalManagers,
        long totalEmployees,

        long enabledUsers,
        long disabledUsers,
        long lockedUsers,

        long totalProjects,
        long planningProjects,
        long activeProjects,
        long onHoldProjects,
        long completedProjects,
        long cancelledProjects,

        long totalProjectMembers
    ) {}

    public record RecentUser(
      UUID id,
      String fullName,
      String email,
      Role role,
      boolean enabled
    ){}

    public record RecentProject(
            UUID id,
            String name,
            ProjectStatus status,
            String managerName,
            long memberCount
    ) {}

}


