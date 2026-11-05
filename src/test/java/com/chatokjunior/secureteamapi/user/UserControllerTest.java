package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.auth.service.CustomUserDetailsService;
import com.chatokjunior.secureteamapi.config.SecurityConfig;
import com.chatokjunior.secureteamapi.exception.UserAlreadyExistsException;
import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.security.jwt.JwtService;
import com.chatokjunior.secureteamapi.user.dto.CreateUserRequest;
import com.chatokjunior.secureteamapi.user.dto.CreateUserResponse;
import com.chatokjunior.secureteamapi.user.dto.MyProfileResponse;
import com.chatokjunior.secureteamapi.user.dto.RoleUpdateRequest;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
import lombok.With;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import javax.print.attribute.standard.Media;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    // ====================
    // Test Methods
    // ====================

    @Test
    @WithMockUser
    void getUser_whenAuthenticated_returns200AndUserDetails() throws Exception {
        UUID id = UUID.randomUUID();
        GetUserDetailsDto dto = new GetUserDetailsDto(
                id,
                "Chatok Junior",
                "chatok@test.com",
                Role.EMPLOYEE,
                true,
                true,
                Instant.now(),
                Instant.now()
        );

        when(userService.getUserBYId(id)).thenReturn(dto);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("chatok@test.com"))
                .andExpect(jsonPath("$.fullName").value("Chatok Junior"));

    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getUsers_whenNotAdmin_returns403() throws Exception{

        mockMvc.perform(get("/api/users"))
                        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_whenAdmin_returns200() throws Exception{

        when(userService.getUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        verify(userService).getUsers(any(Pageable.class));
    }

    // createUser
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenAdminAndValidBody_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        CreateUserResponse response = CreateUserResponse.builder()
                .id(id)
                .fullName("Chatok Junior")
                .email("chatok@test.com")
                .role(Role.EMPLOYEE)
                .build();

        when(userService.createNewUser(any(CreateUserRequest.class))).thenReturn(response);

        String body = """
                {
                    "fullName": "Chatok Junior",
                    "email": "chatok@test.com",
                    "password": "Password123",
                    "role": "EMPLOYEE"
                }
                """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("chatok@test.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenEmailMissing_returns400() throws Exception{

        String body = """
                {
                    "fullName":"Chatok Junior",
                    "password":"Password123",
                    "role":"EMPLOYEE"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                        .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_whenNotAuthenticated_returnsUnauthorized() throws Exception{
        mockMvc.perform(post("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenPasswordTooShort_returns400() throws Exception{
        String body = """
                {
                    "fullName":"Chatok Junior",
                    "password":"short",
                    "email":"chatok@test.com"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenEmailAlreadyExists_returns409() throws Exception{

        String body = """
                {
                    "fullName":"Chatok Junior",
                    "password":"Password123",
                    "email":"chatok@test.com"
                }
                """;

        when(userService.createNewUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    // updateUserRole
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenAdminAndValidBody_returns200() throws Exception {
        String body = """
                {
                    "role":"MANAGER"
                }
                """;
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        verify(userService).updateUserRole(eq(id), any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserRole_whenNotAdmin_returns403() throws Exception{

        UUID id = UUID.randomUUID();

        String body = """
                {
                    "role":"MANAGER"
                }
                """;

        mockMvc.perform(patch("/api/users/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUserRole(eq(id), any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenIdIsNotUuid_returns400() throws Exception{
        String id = "123454";
        String body = """
                {
                    "role":"MANAGER"
                }
                """;

        mockMvc.perform(patch("/api/users/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenUserNotFound_returns404() throws Exception{
        UUID id = UUID.randomUUID();

        String body = """
                {
                    "role":"MANAGER"
                }
                """;

        doThrow(new UserNotFoundException("User not found"))
                .when(userService).updateUserRole(eq(id), any(RoleUpdateRequest.class));

        mockMvc.perform(patch("/api/users/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        verify(userService).updateUserRole(eq(id), any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenRoleValueIsInvalid_returns400() throws Exception{
        UUID id = UUID.randomUUID();

        String body = """
                {
                    "role":"SUPERHERO"
                }
                """;

        mockMvc.perform(patch("/api/users/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenRoleMissing_returns400() throws Exception{
        UUID id = UUID.randomUUID();

        String body = """
                {
                }
                """;

        mockMvc.perform(patch("/api/users/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // disableUserById
    @Test
    @WithMockUser(roles = "ADMIN")
    void disableUserById_whenAdmin_returns200AndMessage() throws Exception {

        UUID id = UUID.randomUUID();

        when(userService.disableUserById(id)).thenReturn("User Has Been Disable Successfully");

        mockMvc.perform(patch("/api/users/{id}/disable", id))
                .andExpect(status().isOk())
                .andExpect(content().string("User Has Been Disable Successfully"));

        verify(userService).disableUserById(id);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void disableUserById_whenNotAdmin_returns403() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{id}/disable", id))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void disableUserById_whenUserNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.disableUserById(id))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(patch("/api/users/{id}/disable", id))
                .andExpect(status().isNotFound());

        verify(userService).disableUserById(id);
    }

    // myProfile
    @Test
    @WithMockUser(username = "chatok@test.com", roles = "EMPLOYEE")
    void myProfile_whenAuthenticated_returns200AndProfile() throws Exception {

        MyProfileResponse response = MyProfileResponse.builder()
                .fullName("Chatok Junior")
                .email("chatok@test.com")
                .role(Role.EMPLOYEE)
                .build();

        when(userService.myProfile(any(Authentication.class))).thenReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("chatok@test.com"))
                .andExpect(jsonPath("$.fullName").value("Chatok Junior"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));

        verify(userService).myProfile(argThat(auth -> auth.getName().equals("chatok@test.com")));
    }

    @Test
    void myProfile_whenNotAuthenticated_returnsUnauthorized() throws Exception{

        mockMvc.perform(get("/api/users/me"))
                        .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }
}
