package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.user.dto.ChangePasswordRequest;
import com.chatokjunior.secureteamapi.user.dto.GetAllUserResponse;
import com.chatokjunior.secureteamapi.user.dto.MyProfileResponse;
import com.chatokjunior.secureteamapi.user.repository.projections.GetAllUsersDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;


@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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
}
