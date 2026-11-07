package com.chatokjunior.secureteamapi;

import com.chatokjunior.secureteamapi.config.TestcontainersConfig;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@Transactional
public class AuthFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "Password123";

    private void createUser(String email, Role role) {
        userRepository.save(
                User.builder()
                        .fullName("Test User")
                        .email(email)
                        .password(passwordEncoder.encode(PASSWORD))
                        .role(role)
                        .isEmailVerified(true)
                        .build()
        );
    }

    private Cookie[] login(String email) throws Exception{
        String body = """
                {
                    "email":"%s",
                    "password":"%s"
                }
                """.formatted(email, PASSWORD);

        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookies();
    }

    @Test
    void login_withInvalidEmail_return400() throws Exception{
        createUser("chatok@test.com", Role.EMPLOYEE);

        String body = """
                {
                    "email":"chatokTeste.com",
                    "password":"Password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("must be a well-formed email address"));
    }

    @Test
    void login_withInvalidPassword_return400() throws Exception {
        createUser("chatok@test.com", Role.EMPLOYEE);

        String body = """
                {
                    "email":"chatok@test.com",
                    "password":"pass"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("size must be between 8 and 20"));
    }

    @Test
    void login_withInvalidEmailAndPassword_return400WithMessage() throws Exception {
        createUser("chatok@test.com", Role.EMPLOYEE);

        String body = """
                {
                    "email":"chatok.com",
                    "password":"pass"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("must be a well-formed email address"))
                .andExpect(jsonPath("$.errors.password").value("size must be between 8 and 20"));
    }

    @Test
    void login_whenCredentialsValid_setsAccessAndRefreshCookies() throws Exception {
        createUser("chatok@test.com", Role.EMPLOYEE);

        String body = """
                {
                    "email":"chatok@test.com",
                    "password":"Password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void me_whenLoggedInWithRealCookies_returnsOwnProfile() throws Exception {
        createUser("chatok@test.com", Role.EMPLOYEE);
        Cookie[] cookies = login("chatok@test.com");

        mockMvc.perform(get("/api/users/me").cookie(cookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("chatok@test.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void me_whenNoCookies_returnsUnauthorized() throws Exception{
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsers_whenLoggedInAsEmployee_returnsForbidden() throws Exception{

        createUser("chatok@test.com", Role.EMPLOYEE);
        Cookie[] cookies = login("chatok@test.com");

        mockMvc.perform(get("/api/users")
                        .cookie(cookies))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_whenLoggedInAsAdmin_returnsOk() throws Exception{
        createUser("chatok@test.com", Role.ADMIN);
        Cookie[] cookies = login("chatok@test.com");

        mockMvc.perform(get("/api/users")
                .cookie(cookies))
                .andExpect(status().isOk());
    }
}
