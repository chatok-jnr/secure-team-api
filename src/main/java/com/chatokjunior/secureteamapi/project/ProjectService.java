package com.chatokjunior.secureteamapi.project;

import com.chatokjunior.secureteamapi.auth.service.CustomUserDetails;
import com.chatokjunior.secureteamapi.exception.*;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectRequest;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectResponse;
import com.chatokjunior.secureteamapi.project.entity.Project;
import com.chatokjunior.secureteamapi.project.entity.ProjectMember;
import com.chatokjunior.secureteamapi.project.repository.ProjectMemberRepository;
import com.chatokjunior.secureteamapi.project.repository.ProjectRepository;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

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

    @Transactional
    public String addProjectMember(UUID projectId, UUID employeeId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project Not Found"));

        if(!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(project.getManager().getId())) {
            throw new ForbiddenException("You are not authorized to perform this operation");
        }

        boolean isExist = projectMemberRepository.existsByProjectIdAndEmployeeId(projectId, employeeId);
        if(isExist) {
            throw new ProjectMemberAlreadyExistsException("This Member is Already in this Project");
        }

        ProjectMember projectMember = ProjectMember.builder()
                .employee(employee)
                .project(project)
                .build();

        projectMemberRepository.save(projectMember);

        return "Member Added Successfully";
    }

    @Transactional
    public String removeProjectMember(UUID projectId, UUID employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project Not Found"));

        if(!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(project.getManager().getId())) {
            throw new ForbiddenException("You are not authorized to perform this operation");
        }

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndEmployeeId(projectId, employeeId)
                .orElseThrow(() -> new ProjectMemberNotFoundException("This User Is Not A Member Of This Project"));

        projectMemberRepository.delete(projectMember);

        return "Member is Removed Successfully From this Project";
    }
}
