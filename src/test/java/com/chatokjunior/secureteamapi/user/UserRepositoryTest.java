package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;
import java.util.UUID;

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

    @Test
    void getUserById_whenUserExist_returnsTrue() {

        User user = saveUser("chatok@test.com", Role.EMPLOYEE);

        Optional<GetUserDetailsDto> result = userRepository.getUserById(user.getId());

        assertThat(result.get().id()).isEqualTo(user.getId());
        assertThat(result.get().fullName()).isEqualTo(user.getFullName());
        assertThat(result.get().email()).isEqualTo(user.getEmail());
        assertThat(result.get().role()).isEqualTo(user.getRole());
        assertThat(result.get().enabled()).isEqualTo(user.isEnabled());
        assertThat(result.get().accountNonLocked()).isEqualTo(user.isAccountNonLocked());
        assertThat(result.get().createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(result.get().updatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void getUserById_whenUserDoesNotExist_returnsEmpty() {
        UUID id = UUID.randomUUID();
        Optional<GetUserDetailsDto> result = userRepository.getUserById(id);

        assertThat(result).isEqualTo(Optional.empty());
    }
}
