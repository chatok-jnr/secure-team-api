package com.chatokjunior.secureteamapi.admin;

import com.chatokjunior.secureteamapi.admin.dto.AdminDashboardResponse;
import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;
import com.chatokjunior.secureteamapi.project.repository.ProjectMemberRepository;
import com.chatokjunior.secureteamapi.project.repository.ProjectRepository;
import com.chatokjunior.secureteamapi.project.repository.projection.StatusCount;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import com.chatokjunior.secureteamapi.user.repository.projections.EnableCountProjection;
import com.chatokjunior.secureteamapi.user.repository.projections.RoleCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {

        Map<Role, Long> roles = new EnumMap<>(Role.class);
        for(Role r: Role.values()) roles.put(r, 0L);
        userRepository.countByRole()
                .forEach(row -> roles.put(row.getRole(), row.getCount()));
        long totalUsers = roles.values().stream().mapToLong(Long::longValue).sum();

        long enabledUsers = 0, disabledUsers = 0;
        for(EnableCountProjection e: userRepository.countEnabledUser()) {
            if(Boolean.TRUE.equals(e.getEnabled())) enabledUsers = e.getCount();
            else disabledUsers = e.getCount();
        }

        long lockedUsers = userRepository.lockedUser();

        Map<ProjectStatus, Long> statuses = new EnumMap<>(ProjectStatus.class);
        for(ProjectStatus s:ProjectStatus.values()) statuses.put(s, 0L);
        projectRepository.countProjectByStatus()
                .forEach(row -> statuses.put(row.getStatus(), row.getCount()));
        long totalProjects = statuses.values().stream().mapToLong(Long::longValue).sum();

        long totalProjectMembers = projectMemberRepository.count();

        List<AdminDashboardResponse.RecentUser> recentUsers = userRepository
                .getRecentUsers(PageRequest.of(0, 3))
                .stream()
                .map(r -> new AdminDashboardResponse.RecentUser(
                        r.id(), r.fullName(), r.email(),
                        r.role(), Boolean.TRUE.equals(r.enabled())))
                .toList();

        List<AdminDashboardResponse.RecentProject> recentProjects = projectRepository
                .getRecentProject(PageRequest.of(0, 3))
                .stream()
                .map(r -> new AdminDashboardResponse.RecentProject(
                        r.id(), r.name(), r.status(),
                        r.managerName(), r.memberCount()
                ))
                .toList();

        AdminDashboardResponse.Summary summary = new AdminDashboardResponse.Summary(
            totalUsers,
            roles.get(Role.ADMIN),
            roles.get(Role.MANAGER),
            roles.get(Role.EMPLOYEE),

            enabledUsers,
            disabledUsers,
            lockedUsers,

            totalProjects,
            statuses.get(ProjectStatus.PLANNING),
            statuses.get(ProjectStatus.ACTIVE),
            statuses.get(ProjectStatus.ON_HOLD),
            statuses.get(ProjectStatus.COMPLETED),
            statuses.get(ProjectStatus.CANCELLED),

            totalProjectMembers
        );

        return new AdminDashboardResponse(
                summary,
                recentUsers,
                recentProjects
        );
    }
}
