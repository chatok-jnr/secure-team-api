package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.user.dto.ChangePasswordRequest;
import com.chatokjunior.secureteamapi.user.dto.MyProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



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
}
