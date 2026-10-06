package com.chatokjunior.secureteamapi.auth.service;

import com.chatokjunior.secureteamapi.exception.AccountLockedException;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication)
    throws AuthenticationException {
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        CustomUserDetails userDetails;

        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        } catch (Exception ex) {
            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        if(!userDetails.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        if(!userDetails.isAccountNonLocked()) {
            throw new LockedException("Your account has been locked due to multiple failed login attempts.\n" + //
                                "Please contact our support team to unlock your account.");
        }

        if(!passwordEncoder.matches(
                rawPassword,
                userDetails.getPassword()
        )) {
            handleFailedLogin(userDetails);

            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        handleSuccessfulLogin(userDetails);

        return UsernamePasswordAuthenticationToken.authenticated(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Transactional 
    private void handleFailedLogin(CustomUserDetails userDetails) {

        var user = userDetails.getUser();
        int attempts = user.getFailedLoginAttempts() + 1;
        
        user.setFailedLoginAttempts(attempts);

        if(user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
        }

        userRepository.save(user);
    }

    @Transactional 
    private void handleSuccessfulLogin(CustomUserDetails userDetails) {
        var user = userDetails.getUser();

        if(user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
