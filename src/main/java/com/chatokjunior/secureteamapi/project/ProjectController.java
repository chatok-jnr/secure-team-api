package com.chatokjunior.secureteamapi.project;

import com.chatokjunior.secureteamapi.project.dto.CreateProjectRequest;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectResponse;
import com.chatokjunior.secureteamapi.project.entity.Project;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectMemberProjection;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectResponseProjection;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CreateProjectResponse> createProject(
            @RequestBody
            @Valid
            CreateProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PostMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> addProjectMember(
            @PathVariable("projectId")
            UUID projectId,
            @PathVariable("userId")
            UUID userId
    ) {
        return ResponseEntity.ok(projectService.addProjectMember(projectId, userId));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> removeProjectMember(
            @PathVariable("projectId")
            UUID projectId,
            @PathVariable("memberId")
            UUID memberId
    ) {
        return ResponseEntity.ok(projectService.removeProjectMember(projectId, memberId));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberProjection>> getProjectMembers(
            @PathVariable("projectId")
            UUID projectId
    ) {
        return ResponseEntity.ok(projectService.getProjectMembers(projectId));
    }

    @GetMapping()
    public ResponseEntity<Page<ProjectResponseProjection>> getProjects(
            @RequestParam(value = "page", defaultValue = "0")
            int page,
            @RequestParam(value = "size", defaultValue = "1")
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(projectService.getProjects(pageable));
    }
}
