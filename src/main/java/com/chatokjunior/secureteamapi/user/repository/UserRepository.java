package com.chatokjunior.secureteamapi.user.repository;

import com.chatokjunior.secureteamapi.user.entity.User;

import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
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

}

