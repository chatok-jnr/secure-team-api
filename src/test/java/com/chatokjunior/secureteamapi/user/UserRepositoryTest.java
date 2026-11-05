package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private UserRepository userRepository;

    private User saveUser(String email, Role role) {
        return userRepository.save(User.builder()
                        .fullName("Chatok Junior")
                        .email("chatok@test.com")
                        .password("hash")
                        .role(role)
                .build());
    }

    @Test
    void existByEmail_whenUserExist_returnsTrue() {
        saveUser("chatok@test.com", Role.EMPLOYEE);

        assertThat(userRepository.existsByEmail("chatok@test.com")).isTrue();
    }

    @Test
    void existByEmail_whenUserDoesNotExist_returnFalse() {
        assertThat(userRepository.existsByEmail("chatok@test.com")).isFalse();
    }

    @Test
    void findByEmail_whenUserExist_returnsUser() {
        saveUser("chatok@test.com", Role.EMPLOYEE);

        Optional<User> user = userRepository.findByEmail("chatok@test.com");

        assertThat(user).isPresent();
        assertThat(user.get().getFullName()).isEqualTo("Chatok Junior");
        assertThat(user.get().getEmail()).isEqualTo("chatok@test.com");
        assertThat(user.get().getRole()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void findByEmail_whenUserDoesNotExist_returnEmpty() {
        Optional<User> user = userRepository.findByEmail("chatok@test.com");

        assertThat(user).isEqualTo(Optional.empty());
    }
}
