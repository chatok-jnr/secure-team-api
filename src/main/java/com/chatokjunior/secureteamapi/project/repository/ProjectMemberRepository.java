package com.chatokjunior.secureteamapi.project.repository;

import com.chatokjunior.secureteamapi.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
}
