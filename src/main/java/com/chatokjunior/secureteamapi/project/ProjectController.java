package com.chatokjunior.secureteamapi.project;

import com.chatokjunior.secureteamapi.project.dto.CreateProjectRequest;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

}
