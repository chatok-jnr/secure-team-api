package com.chatokjunior.secureteamapi.project.repository;

import com.chatokjunior.secureteamapi.project.entity.ProjectMember;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectMemberProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    Optional<ProjectMember> findByProjectIdAndMemberId(UUID projectId, UUID memberId);

    @Query("""
        SELECT  u.id AS id,
                u.fullName as fullName,
                u.email as email,
                u.role as role,
                u.enabled as enabled
        FROM ProjectMember pm
        JOIN pm.member u
        WHERE pm.project.id = :projectId
""")
    List<ProjectMemberProjection> getProjectMembersByProjectId(UUID projectId);

    @Query("""
    select count(distinct pm.member.id)
    from ProjectMember pm
    where pm.project.manager.id = :managerId
""")
    long countDistinctMembersByManagerId(UUID managerId);

    boolean existsByProjectIdAndMemberId(UUID projectId, UUID memberId);
}
