package com.chatokjunior.secureteamapi.project;

import com.chatokjunior.secureteamapi.auth.service.CustomUserDetails;
import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectRequest;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectResponse;
import com.chatokjunior.secureteamapi.project.entity.Project;
import com.chatokjunior.secureteamapi.project.repository.ProjectRepository;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public CreateProjectResponse createProject(CreateProjectRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User manager = userDetails.getUser();

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .manager(manager)
                .build();

        Project newProject = projectRepository.save(project);

        CreateProjectResponse response = CreateProjectResponse.builder()
                .id(newProject.getId())
                .name(newProject.getName())
                .description(newProject.getDescription())
                .status(newProject.getStatus())
                .managerId(manager.getId())
                .createdAt(newProject.getCreatedAt())
                .build();

        return  response;
    }
}
