package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.auth.service.AuthSessionService;
import com.chatokjunior.secureteamapi.auth.service.CustomUserDetails;
import com.chatokjunior.secureteamapi.exception.PasswordMismatchedException;
import com.chatokjunior.secureteamapi.exception.UserAlreadyExistsException;
import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.refresh.RefreshToken;
import com.chatokjunior.secureteamapi.refresh.RefreshTokenRepository;
import com.chatokjunior.secureteamapi.user.dto.*;
import com.chatokjunior.secureteamapi.user.entity.Role;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import com.chatokjunior.secureteamapi.user.repository.projections.GetAllUsersDto;
import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthSessionService authSessionService;

    @Transactional
    public CreateUserResponse createNewUser(CreateUserRequest request) {

        System.out.println("\n\nDebug");

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        System.out.println(username);
        System.out.println(authentication.getAuthorities());

        System.out.println("\n");

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        boolean userExist = userRepository.existsByEmail(request.getEmail());

        if(userExist) {
            throw new UserAlreadyExistsException("You already have an account with this email");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(hashedPassword)
                .role(request.getRole())
                .build();

        User newUser = userRepository.save(user);

        CreateUserResponse newUserResponse = CreateUserResponse.builder()
                .id(newUser.getId())
                .fullName(newUser.getFullName())
                .email(newUser.getEmail())
                .role(newUser.getRole())
                .enabled(newUser.isEnabled())
                .accountNonLocked(newUser.isAccountNonLocked())
                .createdAt(newUser.getCreatedAt())
                .build();

        return newUserResponse;
    }

    @Transactional
    public Void updateUserRole(UUID id, RoleUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getRole().equals(request.getRole())) return null;

        user.setRole(request.getRole());
        userRepository.save(user);

        return null;
    }

    public Page<GetAllUsersDto> getUsers(Pageable pageable) {

        Page<GetAllUsersDto> users = userRepository.getAllUser(pageable);

        return users;
    }

    public GetUserDetailsDto getUserBYId(UUID id) {
        GetUserDetailsDto user = userRepository.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user;
    }

    public MyProfileResponse myProfile(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with this email not found: " + email));

        MyProfileResponse myProfile = MyProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return myProfile;
    }

    @Transactional
    public String changePassword(
            HttpServletResponse httpResponse,
            ChangePasswordRequest request
    ) {
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with this email: " + email + " not found"));

        boolean isCurrentPassword = passwordEncoder.matches(currentPassword, user.getPassword());

        if(!isCurrentPassword) {
            throw new PasswordMismatchedException("Your current Password is incorrect");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        userRepository.save(user);

        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUser(user);

        if(!refreshTokens.isEmpty()) {
            for(RefreshToken refreshToken: refreshTokens) {
                if(!refreshToken.isRevoked()) {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                }
            }
        }

        authSessionService.addAccessToken(httpResponse, user.getEmail());
        authSessionService.addRefreshToken(httpResponse, user.getEmail());

        return "Password updated successfully";
    }
}
