package com.chatokjunior.secureteamapi.manager;

import com.chatokjunior.secureteamapi.auth.service.CustomUserDetails;
import com.chatokjunior.secureteamapi.manager.dto.ManagerDashboardResponse;
import com.chatokjunior.secureteamapi.project.entity.ProjectStatus;
import com.chatokjunior.secureteamapi.project.repository.ProjectMemberRepository;
import com.chatokjunior.secureteamapi.project.repository.ProjectRepository;
import com.chatokjunior.secureteamapi.project.repository.projection.RecentProjectRow;
import com.chatokjunior.secureteamapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public ManagerDashboardResponse getDashboard() {
        User manager = currentUser();
        Map<ProjectStatus, Long> counts = new EnumMap<>(ProjectStatus.class);
        for(ProjectStatus s:ProjectStatus.values()) {
            counts.put(s, 0L);
        }

        projectRepository.countByStatusForManager(currentUser().getId())
                .forEach(row -> counts.put(row.getStatus(), row.getCount()));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long totalMembers = projectMemberRepository.countDistinctMembersByManagerId(currentUser().getId());
        List<ManagerDashboardResponse.RecentProject> recent =
                projectRepository.findRecentWithMemberCount(currentUser().getId(), PageRequest.of(0, 5));

        return new ManagerDashboardResponse(
                new ManagerDashboardResponse.ManagerInfo(manager.getId(), manager.getFullName(), manager.getEmail()),
                new ManagerDashboardResponse.Summary(total, counts, totalMembers),
                recent
        );
    }

    // Helper Function
    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
