package com.chatokjunior.secureteamapi.project.repository;

import com.chatokjunior.secureteamapi.manager.dto.ManagerDashboardResponse;
import com.chatokjunior.secureteamapi.project.entity.Project;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectDetailsProjection;
import com.chatokjunior.secureteamapi.project.repository.projection.ProjectResponseProjection;
import com.chatokjunior.secureteamapi.project.repository.projection.RecentProjectRow;
import com.chatokjunior.secureteamapi.project.repository.projection.StatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
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

    @Query("""
        select 
                p.id as id,
                p.name as name,
                p.description as description,
                p.manager.id as managerId,
                p.manager.fullName as managerName,
                p.manager.email as managerEmail,
                p.createdAt as createdAt,
                p.updatedAt as updatedAt
        from Project p
        where p.id = :id
    """)
    Optional<ProjectDetailsProjection> getProjectDetailsById(UUID id);

    @Query("""
    select 
        pm.project.id as id,
        pm.project.name as name,
        pm.project.description as description,
        pm.project.manager.id as managerId,
        pm.project.manager.fullName as managerName,
        pm.project.manager.email as mangerEmail,
        pm.project.createdAt as createdAt,
        pm.project.updatedAt as updatedAt
    from 
        ProjectMember pm
    where 
        pm.member.id = :memberId and pm.project.id = :id
    """)
    Optional<ProjectDetailsProjection> getProjectDetailsByIdForMember(UUID id, UUID memberId);

    @Query("""
    select p.status as status, count(p) as count
    from Project p
    where p.manager.id = :managerId
    group by p.status
""")
    List<StatusCount> countByStatusForManager(UUID managerId);

    @Query("""
        select 
            p.id as id,
            p.name as name,
            p.status as status,
            count(pm) as memberCount
        from Project p
        left join ProjectMember pm on pm.project = p
        where p.manager.id = :managerId
        group by p.id, p.name, p.status, p.createdAt
        order by p.createdAt desc
    """)
    List<ManagerDashboardResponse.RecentProject> findRecentWithMemberCount(UUID managerId, Pageable pageable);
}
