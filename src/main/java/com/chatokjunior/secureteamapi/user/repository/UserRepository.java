package com.chatokjunior.secureteamapi.user.repository;

import com.chatokjunior.secureteamapi.admin.dto.AdminDashboardResponse;
import com.chatokjunior.secureteamapi.user.entity.User;

import com.chatokjunior.secureteamapi.user.repository.projections.EnableCountProjection;
import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
import com.chatokjunior.secureteamapi.user.repository.projections.RoleCountProjection;
import org.springframework.data.domain.Pageable;
import java.util.*;

import com.chatokjunior.secureteamapi.user.repository.projections.GetAllUsersDto;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User>findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = """
    select 
    new com.chatokjunior.secureteamapi.user.repository.projections.GetAllUsersDto(
        u.id, u.email, u.fullName, u.role
    ) 
    from User u
""",
    countQuery = "select count(u) from User u")
    Page<GetAllUsersDto> getAllUser(Pageable pageable);

    @Query("""
    select 
    new com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto(
        u.id, u.fullName, u.email, u.role, u.enabled, u.accountNonLocked, u.createdAt, u.updatedAt
    ) 
    from User u
    where u.id = :id
""")
    Optional<GetUserDetailsDto> getUserById(UUID id);

    // Admin Dashboard
    @Query("""
    select u.role as role, count(u.id) as count
    from User u
    group by u.role
""")
    List<RoleCountProjection> countByRole();

    @Query("""
    select u.enabled as enabled, count(u.id) as count
    from User u
    group by u.enabled
""")
    List<EnableCountProjection> countEnabledUser();

    @Query("""
    select count(u.id) as count
    from User u
    where u.accountNonLocked = false
""")
    long lockedUser();

    @Query("""
    select u.id as id, u.fullName as fullName, u.email as email, u.role as role, u.enabled as enabled
    from User u
    order by u.createdAt desc
""")
    List<AdminDashboardResponse.RecentUser> getRecentUsers(Pageable pageable);
}

