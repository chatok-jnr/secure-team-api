package com.chatokjunior.secureteamapi.project.repository;

import com.chatokjunior.secureteamapi.project.entity.Project;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectResponseProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query(value = """
    select 
            p.id as id,
            p.name as name,
            p.description as description
    from Project p
    """,
    countQuery = "select count(p) from Project p")
    Page<ProjectResponseProjection> getProjects(Pageable pageable);

    @Query( value = """
    select 
            p.id as id,
            p.name as name,
            p.description as description
    from Project p
    where p.manager.id = :managerId
""",
    countQuery = """
    select count(p) from Project p
    where p.manager.id = :managerId
""")
    Page<ProjectResponseProjection> getProjectsByManagerId(UUID managerId, Pageable pageable);

    @Query( value = """
    select 
            pm.project.id as id,
            pm.project.name as name,
            pm.project.description as description
         from ProjectMember pm
        where pm.member.id = :memberId
    """,
    countQuery = """
    select count(pm) from ProjectMember pm
    where pm.member.id = :memberId
"""
    ) Page<ProjectResponseProjection> getProjectsByMemberId(UUID memberId, Pageable pageable);
}
