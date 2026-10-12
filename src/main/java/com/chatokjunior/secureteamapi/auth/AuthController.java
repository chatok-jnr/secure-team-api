package com.chatokjunior.secureteamapi.auth;

import com.chatokjunior.secureteamapi.auth.dto.*;
import com.chatokjunior.secureteamapi.auth.service.AuthService;
import com.chatokjunior.secureteamapi.auth.service.SessionService;
import com.chatokjunior.secureteamapi.notification.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final SessionService sessionService;

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponse> createNewUser(
            @RequestBody
            CreateUserRequest req
    ) {
        return ResponseEntity.ok(authService.createUser(req));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(
            @RequestBody
            @Valid
            LoginUserRequest request,
            HttpServletResponse response
    ) {
        authService.login(request, response);
        return ResponseEntity.ok("Login Successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        authService.refresh(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody
            @Valid
            ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            HttpServletResponse httpResponse,

            @RequestBody
            @Valid
            ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.resetPassword(httpResponse, request));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutAll(
            HttpServletResponse httpResponse
    ) {
        authService.logoutAll(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionResponse>> getSessions(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                sessionService.getSessions(authentication.getName())
        );
    }

    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> revokeSession(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        sessionService.revokeSession(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public Void test() {

//        emailService.sendMail(
//                "sakib.0000.ansary@gmail.com",
//                "Testing if our thing is working or not?",
//                "This is the body of the mail"
//        );

        return null;
    }
}
