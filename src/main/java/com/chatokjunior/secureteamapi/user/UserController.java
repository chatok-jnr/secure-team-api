package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.user.dto.*;
import com.chatokjunior.secureteamapi.user.repository.projections.GetAllUsersDto;
import com.chatokjunior.secureteamapi.user.repository.projections.GetUserDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.UUID;


@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateUserResponse> createUser(
            @RequestBody
            @Valid
            CreateUserRequest request
    ) {
        return ResponseEntity.ok(userService.createNewUser(request));
    }

    @GetMapping("/me")
    public MyProfileResponse myProfile(Authentication authentication){
        return userService.myProfile(authentication);
    }

    @PatchMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody
            @Valid
            ChangePasswordRequest request,
            HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(userService.changePassword(httpResponse, request));
    }

    @GetMapping ()
    public ResponseEntity<Page<GetAllUsersDto>> getUsers(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "10")
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetUserDetailsDto> getUser(
            @PathVariable
            UUID id
    ) {
        return ResponseEntity.ok(userService.getUserBYId(id));
    }
}
