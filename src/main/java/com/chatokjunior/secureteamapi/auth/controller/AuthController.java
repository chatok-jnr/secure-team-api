package com.chatokjunior.secureteamapi.auth.controller;

import com.chatokjunior.secureteamapi.auth.dto.CreateUserRequest;
import com.chatokjunior.secureteamapi.auth.dto.CreateUserResponse;
import com.chatokjunior.secureteamapi.auth.dto.LoginUserRequest;
import com.chatokjunior.secureteamapi.auth.dto.LoginUserResponse;
import com.chatokjunior.secureteamapi.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponse> createNewUser(
            @RequestBody
            CreateUserRequest req
    ) {
        return ResponseEntity.ok(userService.createUser(req));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> loginUser(
            @RequestBody
            @Valid
            LoginUserRequest req
    ) {
        return ResponseEntity.ok(userService.loginUser(req));
    }
}
