package com.chatokjunior.secureteamapi.project;

import com.chatokjunior.secureteamapi.project.dto.CreateProjectRequest;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
