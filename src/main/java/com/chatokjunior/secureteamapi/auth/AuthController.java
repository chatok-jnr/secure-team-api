package com.chatokjunior.secureteamapi.auth;

import com.chatokjunior.secureteamapi.auth.dto.CreateUserRequest;
import com.chatokjunior.secureteamapi.auth.dto.CreateUserResponse;
import com.chatokjunior.secureteamapi.auth.dto.ForgotPasswordRequest;
import com.chatokjunior.secureteamapi.auth.dto.LoginUserRequest;
import com.chatokjunior.secureteamapi.auth.service.AuthService;
import com.chatokjunior.secureteamapi.notification.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

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

    @GetMapping("/test")
    public Void resetPassword() {

//        emailService.sendMail(
//                "sakib.0000.ansary@gmail.com",
//                "Testing if our thing is working or not?",
//                "This is the body of the mail"
//        );

        return null;
    }
}
