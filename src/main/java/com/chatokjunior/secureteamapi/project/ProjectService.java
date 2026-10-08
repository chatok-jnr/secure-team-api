package com.chatokjunior.secureteamapi.project;

import com.chatokjunior.secureteamapi.auth.service.CustomUserDetails;
import com.chatokjunior.secureteamapi.exception.*;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectRequest;
import com.chatokjunior.secureteamapi.project.dto.CreateProjectResponse;
import com.chatokjunior.secureteamapi.project.dto.ProjectUpdateRequest;
import com.chatokjunior.secureteamapi.project.entity.Project;
import com.chatokjunior.secureteamapi.project.entity.ProjectMember;
import com.chatokjunior.secureteamapi.project.repository.ProjectMemberRepository;
import com.chatokjunior.secureteamapi.project.repository.ProjectRepository;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectDetailsProjection;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectMemberProjection;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectResponseProjection;
import com.chatokjunior.secureteamapi.project.dto.ProjectResponse;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public CreateProjectResponse createProject(CreateProjectRequest request) {
        User manager = currentUser();

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .manager(manager)
                .build();

        Project newProject = projectRepository.save(project);
        projectRepository.flush();

        System.out.println(newProject.getCreatedAt());

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
    public String addProjectMember(UUID projectId, UUID memberId) {
        User currentUser = currentUser();

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project Not Found"));

        if(!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(project.getManager().getId())) {
            throw new ForbiddenException("You are not authorized to perform this operation");
        }

        boolean isExist = projectMemberRepository.existsByProjectIdAndMemberId(projectId, memberId);
        if(isExist) {
            throw new ProjectMemberAlreadyExistsException("This Member is Already in this Project");
        }

        ProjectMember projectMember = ProjectMember.builder()
                .member(member)
                .project(project)
                .build();

        projectMemberRepository.save(projectMember);

        return "Member Added Successfully";
    }

    @Transactional
    public String removeProjectMember(UUID projectId, UUID memberId) {
        User currentUser = currentUser();

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project Not Found"));

        if(!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(project.getManager().getId())) {
            throw new ForbiddenException("You are not authorized to perform this operation");
        }

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndMemberId(projectId, memberId)
                .orElseThrow(() -> new ProjectMemberNotFoundException("This User Is Not A Member Of This Project"));

        projectMemberRepository.delete(projectMember);

        return "Member is Removed Successfully From this Project";
    }

    public List<ProjectMemberProjection> getProjectMembers(UUID projectId) {

        User currentUser = currentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        if(
                !currentUser.getRole().equals(Role.ADMIN) &&
                !project.getManager().getId().equals(currentUser.getId()) &&
                !projectMemberRepository.existsByProjectIdAndMemberId(projectId, currentUser.getId())
        ) {
            throw new ForbiddenException("You are not authorized to perform this operation");
        }

        List<ProjectMemberProjection> projectMembers = projectMemberRepository.getProjectMembersByProjectId(projectId);
        return projectMembers;
    }

    public Page<ProjectResponseProjection> getProjects(Pageable pageable) {
        User currentUser = currentUser();

        Page<ProjectResponseProjection> projects;

        if(currentUser.getRole().equals(Role.ADMIN)) {
            projects = projectRepository.getProjects(pageable);
        } else if(currentUser.getRole().equals(Role.MANAGER)) {
            projects = projectRepository.getProjectsByManagerId(currentUser.getId(), pageable);
        } else {
            projects = projectRepository.getProjectsByMemberId(currentUser.getId(), pageable);
        }

        return projects;
    }

    public ProjectDetailsProjection getProjectById(UUID id) {
        User currentUser = currentUser();

        ProjectDetailsProjection project;
        if(currentUser.getRole().equals(Role.ADMIN)) {
            project = projectRepository.getProjectDetailsById(id)
                    .orElseThrow(()-> new ProjectNotFoundException("Project not found"));
        } else if(currentUser.getRole().equals(Role.MANAGER)) {
            project = projectRepository.getProjectDetailsById(id)
                    .orElseThrow(()-> new ProjectNotFoundException("Project not found"));

            if(!project.managerId().equals(currentUser.getId())) {
                throw new ForbiddenException("You Don't Have Access To This Project");
            }
        } else {
            project = projectRepository.getProjectDetailsByIdForMember(id, currentUser.getId())
                    .orElseThrow(()-> new ProjectNotFoundException("Project not found"));
        }

        return project;
    }

    @Transactional
    public CreateProjectResponse updateProjectById(UUID id, ProjectUpdateRequest request) {
        User currentUser = currentUser();

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        if(currentUser.getRole().equals(Role.MANAGER) && !currentUser.getId().equals(project.getManager().getId())) {
            throw new ForbiddenException("You are not permitted to perform this action");
        }

        if(request.name() != null) project.setName(request.name());
        if(request.description() != null) project.setDescription(request.description());
        if(request.status() != null) project.setStatus(request.status());
        if(request.managerId() != null && currentUser.getRole().equals(Role.ADMIN)) {
            User newManager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));
            project.setManager(newManager);
        }

        Project updatedProject = projectRepository.save(project);

        return CreateProjectResponse.builder()
                .id(updatedProject.getId())
                .name(updatedProject.getName())
                .description(updatedProject.getName())
                .status(updatedProject.getStatus())
                .managerId(updatedProject.getManager().getId())
                .createdAt(updatedProject.getCreatedAt())
                .build();
    }

    @Transactional
    public Void deleteProjectById(UUID id) {

        Project project = projectRepository.findById(id)
                        .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        projectRepository.delete(project);

        return null;
    }

    // =========================
    // Helper Functions
    // =========================
    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
