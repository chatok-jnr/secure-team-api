package com.chatokjunior.secureteamapi.user.repository;

import com.chatokjunior.secureteamapi.user.entity.User;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User>findByEmail(String email);
    boolean existsByEmail(String email);
}
