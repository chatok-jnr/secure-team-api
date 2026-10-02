package com.chatokjunior.secureteamapi.auth.controller;

import com.chatokjunior.secureteamapi.auth.dto.CreateUserRequest;
import com.chatokjunior.secureteamapi.auth.dto.CreateUserResponse;
import com.chatokjunior.secureteamapi.auth.dto.LoginUserRequest;
import com.chatokjunior.secureteamapi.auth.service.AuthService;
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.noContent().build();
    }
}
