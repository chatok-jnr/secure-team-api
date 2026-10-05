package com.chatokjunior.secureteamapi.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity 
@Table (name="users")
@Getter 
@Setter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="full_name", nullable = false)
    private String fullName;

    @Column (nullable = false, unique = true)
    private String email;

    @Column (nullable = false)
    private String password;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    @Builder.Default
    private Role role = Role.EMPLOYEE;

    @Column (nullable = false)
    @ColumnDefault ("true")
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_non_locked")
    @ColumnDefault("true")
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "failed_login_attempts")
    @ColumnDefault("0")
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "created_at")
    @CreationTimestamp
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
