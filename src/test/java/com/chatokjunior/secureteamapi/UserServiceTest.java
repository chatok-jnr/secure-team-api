package com.chatokjunior.secureteamapi;

import com.chatokjunior.secureteamapi.audit.entity.AuditAction;
import com.chatokjunior.secureteamapi.audit.services.AuditService;
import com.chatokjunior.secureteamapi.exception.UserAlreadyExistsException;
import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.user.dto.CreateUserRequest;
import com.chatokjunior.secureteamapi.user.dto.CreateUserResponse;
import com.chatokjunior.secureteamapi.auth.service.AuthSessionService;
import com.chatokjunior.secureteamapi.refresh.RefreshTokenRepository;
import com.chatokjunior.secureteamapi.user.UserService;
import com.chatokjunior.secureteamapi.user.dto.RoleUpdateRequest;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuthSessionService authSessionService;
    @Mock private AuditService auditService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setupSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    // Test Methods ------------------------------------------------

    // Create new User
    @Test
    void createNewUser_whenEmailIsNew_savesUserAndReturnResponse() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .fullName("Chatok Junior")
                .email("chatok@test.com")
                .password("plain123")
                .role(Role.EMPLOYEE)
                .build();

        UUID id = UUID.randomUUID();

        when(passwordEncoder.encode("plain123")).thenReturn("hash123");
        when(userRepository.existsByEmail("chatok@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(id);
            return u;
        });

        // Act
        CreateUserResponse response = userService.createNewUser(request);

        // Assert
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getRole()).isEqualTo(request.getRole());

        verify(auditService).record(
          eq(AuditAction.USER_CREATED),
                eq("admin@test.com"),
                eq("USER"),
                eq(id.toString()),
                anyString(),
                isNull()
        );
    }

    @Test
    void createNewUser_whenEmailExists_throwsAndDoesNotSave() {
        CreateUserRequest request = CreateUserRequest.builder()
                .fullName("Chatok Junior")
                .email("chatok@test.com")
                .password("plain123")
                .build();

        when(userRepository.existsByEmail("chatok@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createNewUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditService);
    }

    // Update User Role
    @Test
    void updateUserRole_whenRoleIsDifferent_savesUserAndRecordsAudit() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .role(Role.EMPLOYEE)
                .build();

        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.updateUserRole(id, request);

        assertThat(user.getRole()).isEqualTo(Role.MANAGER);
        verify(userRepository).save(user);
        verify(auditService).record(
                eq(AuditAction.ROLE_CHANGED),
                eq("admin@test.com"),
                eq("USER"),
                eq(id.toString()),
                eq("Role changed to MANAGER"),
                isNull()
        );

    }

    @Test
    void updateUserRole_whenUserNotFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();

        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(id, request))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditService);
    }

    @Test
    void updateUserRole_whenRoleIsSame_doesNotSaveOrAudit() {
        UUID id = UUID.randomUUID();

        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole(Role.EMPLOYEE);

        User user = User.builder()
                .id(id)
                .role(Role.EMPLOYEE)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.updateUserRole(id, request);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditService);
    }

    // getUserById
    @Test
    void getUserBYId_whenUserIsFound_returnsUserDetails() {
        UUID id = UUID.randomUUID();
        GetUserDetailsDto response = new GetUserDetailsDto(id, "Chatok Junior", "chatok@test.com", Role.EMPLOYEE,
                true, true, Instant.now(), Instant.now());

        when(userRepository.getUserById(id)).thenReturn(Optional.of(response));

        GetUserDetailsDto user = userService.getUserBYId(id);

        assertThat(user).isSameAs(response);
        verify(userRepository).getUserById(id);
    }
}
