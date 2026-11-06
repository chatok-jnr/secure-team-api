package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.admin.dto.AdminDashboardResponse;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import com.chatokjunior.secureteamapi.user.repository.projections.EnableCountProjection;
import com.chatokjunior.secureteamapi.user.repository.projections.GetAllUsersDto;
import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
import com.chatokjunior.secureteamapi.user.repository.projections.RoleCountProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.Collectors;


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
                        .email(email)
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

    @Test
    void getAllUser_whenUsersExist_returnsSavedUsers() {

        List<String> emails = new ArrayList<>();
        emails.add("user_1@test.com");
        emails.add("user_2@test.com");
        emails.add("user_3@test.com");
        emails.add("user_4@test.com");

        for(String e:emails) saveUser(e, Role.EMPLOYEE);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());

        Page<GetAllUsersDto> result = userRepository.getAllUser(pageable);

        for(int i = 0; i < 4; i++) {
            assertThat(result.getContent().get(i).email()).isEqualTo(emails.get(i));
            assertThat(result.getContent().get(i).role()).isEqualTo(Role.EMPLOYEE);
            assertThat(result.getContent().get(i).fullName()).isEqualTo("Chatok Junior");
        }
    }

    @Test
    void getAllUser_whenUsersExist_returnsCorrectTotalElements() {
        List<String> emails = new ArrayList<>();
        emails.add("user_1@test.com");
        emails.add("user_2@test.com");
        emails.add("user_3@test.com");
        emails.add("user_4@test.com");

        for(String e:emails) saveUser(e, Role.EMPLOYEE);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());

        Page<GetAllUsersDto> result = userRepository.getAllUser(pageable);

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void getAllUser_whenPageSizeIsSmallerThanTotal_returnsOnlyThatManyUsers() {
        List<String> emails = new ArrayList<>();
        emails.add("user_1@test.com");
        emails.add("user_2@test.com");
        emails.add("user_3@test.com");
        emails.add("user_4@test.com");

        for(String e:emails) saveUser(e, Role.EMPLOYEE);

        Pageable pageable = PageRequest.of(0, 2, Sort.by("email").ascending());

        Page<GetAllUsersDto> result = userRepository.getAllUser(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    void getAllUser_whenNoUsersExist_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());

        Page<GetAllUsersDto> result = userRepository.getAllUser(pageable);

        assertThat(result.getContent()).hasSize(0);
    }

    @Test
    void countByRole_whenMixedRoles_returnsCountPerRole() {
        saveUser("user_1@test.com", Role.EMPLOYEE);
        saveUser("user_2@test.com", Role.EMPLOYEE);
        saveUser("user_3@test.com", Role.EMPLOYEE);
        saveUser("user_4@test.com", Role.MANAGER);
        saveUser("user_5@test.com", Role.MANAGER);
        saveUser("user_6@test.com", Role.ADMIN);

        List<RoleCountProjection> result = userRepository.countByRole();

        Map<Role, Long> countMap = result.stream()
                .collect(Collectors.toMap(
                    RoleCountProjection::getRole,
                    RoleCountProjection::getCount
                ));

        assertThat(countMap)
                .containsEntry(Role.EMPLOYEE, 3L)
                .containsEntry(Role.MANAGER, 2L)
                .containsEntry(Role.ADMIN, 1L);
    }

    @Test
    void countByRole_whenNoUsersExist_returnsEmptyList() {
        List<RoleCountProjection> result = userRepository.countByRole();

        assertThat(result).isEmpty();
    }

    @Test
    void countEnabledUser_whenEnabledAndDisabledUsersExist_returnsCountForEach() {
        saveUser("user_1@test.com", Role.EMPLOYEE);
        saveUser("user_2@test.com", Role.EMPLOYEE);
        saveUser("user_3@test.com", Role.EMPLOYEE);
        User user4 = saveUser("user_4@test.com", Role.EMPLOYEE);
        user4.setEnabled(false);
        userRepository.save(user4);

        List<EnableCountProjection> result = userRepository.countEnabledUser();

        Map<Boolean, Long> enableCount = result.stream()
                .collect(Collectors.toMap(
                        EnableCountProjection::getEnabled,
                        EnableCountProjection::getCount
                ));

        assertThat(enableCount).containsExactlyInAnyOrderEntriesOf(Map.of(
                true, 3L,
                false, 1L
        ));
    }

    @Test
    void countEnabledUser_whenAllUsersAreEnabled_returnsOnlyEnabledGroup() {
        saveUser("user_1@test.com", Role.EMPLOYEE);
        saveUser("user_2@test.com", Role.EMPLOYEE);
        saveUser("user_3@test.com", Role.EMPLOYEE);
        saveUser("user_4@test.com", Role.EMPLOYEE);

        List<EnableCountProjection> result = userRepository.countEnabledUser();

        Map<Boolean, Long> countEnableUser = result.stream()
                .collect(Collectors.toMap(
                   EnableCountProjection::getEnabled,
                   EnableCountProjection::getCount
                ));

        assertThat(countEnableUser).containsExactlyEntriesOf(Map.of(true, 4L));
    }

    @Test
    void lockedUser_whenSomeUsersAreLocked_countsOnlyLockedUsers() {
        saveUser("user_1@test.com", Role.EMPLOYEE);

        User user_2 = saveUser("user_2@test.com", Role.EMPLOYEE);
        user_2.setAccountNonLocked(false);
        userRepository.save(user_2);

        saveUser("user_3@test.com", Role.EMPLOYEE);

        User user_4 = saveUser("user_4@test.com", Role.MANAGER);
        user_4.setAccountNonLocked(false);
        userRepository.save(user_4);

        saveUser("user_5@test.com", Role.MANAGER);

        saveUser("user_6@test.com", Role.ADMIN);

        User user_7 = saveUser("user_7@test.com", Role.ADMIN);
        user_7.setAccountNonLocked(false);
        userRepository.save(user_7);

        long lockedUserCount = userRepository.lockedUser();
        assertThat(lockedUserCount).isEqualTo(3);
    }

    @Test
    void lockedUser_whenNoUserIsLocked_returnsZero() {
        saveUser("user_1@test.com", Role.EMPLOYEE);
        saveUser("user_2@test.com", Role.EMPLOYEE);
        saveUser("user_3@test.com", Role.EMPLOYEE);
        saveUser("user_4@test.com", Role.MANAGER);
        saveUser("user_5@test.com", Role.MANAGER);
        saveUser("user_6@test.com", Role.ADMIN);

        long lockedUserCount = userRepository.lockedUser();
        assertThat(lockedUserCount).isEqualTo(0);
    }

    @Test
    void getRecentUsers_whenPageableLimitIsSet_returnsOnlyThatManyUsers() {
        saveUser("user_1@test.com", Role.EMPLOYEE);
        saveUser("user_2@test.com", Role.EMPLOYEE);
        saveUser("user_3@test.com", Role.EMPLOYEE);
        saveUser("user_4@test.com", Role.MANAGER);
        saveUser("user_5@test.com", Role.MANAGER);
        saveUser("user_6@test.com", Role.ADMIN);

        Pageable pageable = PageRequest.of(0, 3);

        List<AdminDashboardResponse.RecentUser> result = userRepository.getRecentUsers(pageable);

        assertThat(result).hasSize(3);
        Map<String, AdminDashboardResponse.RecentUser> recentUsers = result.stream()
                .collect(Collectors.toMap(
                   AdminDashboardResponse.RecentUser::email,
                   ru -> ru
                ));

        assertThat(recentUsers.get("user_6@test.com")).isEqualTo(result.get(0));
        assertThat(recentUsers.get("user_5@test.com")).isEqualTo(result.get(1));
        assertThat(recentUsers.get("user_4@test.com")).isEqualTo(result.get(2));
    }

    @Test
    void getRecentUsers_whenMultipleUsersExist_returnsNewestFirst() {
        saveUser("user_1@test.com", Role.EMPLOYEE);
        saveUser("user_2@test.com", Role.EMPLOYEE);
        saveUser("user_3@test.com", Role.EMPLOYEE);
        saveUser("user_4@test.com", Role.MANAGER);
        saveUser("user_5@test.com", Role.MANAGER);
        saveUser("user_6@test.com", Role.ADMIN);

        Pageable pageable = PageRequest.of(0, 6);

        List<AdminDashboardResponse.RecentUser> result = userRepository.getRecentUsers(pageable);

        Map<String, AdminDashboardResponse.RecentUser> recentUsers = result.stream()
                .collect(Collectors.toMap(
                        AdminDashboardResponse.RecentUser::email,
                        ru -> ru
                ));

        assertThat(recentUsers.get("user_6@test.com")).isEqualTo(result.get(0));
        assertThat(recentUsers.get("user_5@test.com")).isEqualTo(result.get(1));
        assertThat(recentUsers.get("user_4@test.com")).isEqualTo(result.get(2));
        assertThat(recentUsers.get("user_3@test.com")).isEqualTo(result.get(3));
        assertThat(recentUsers.get("user_2@test.com")).isEqualTo(result.get(4));
        assertThat(recentUsers.get("user_1@test.com")).isEqualTo(result.get(5));
    }
}
