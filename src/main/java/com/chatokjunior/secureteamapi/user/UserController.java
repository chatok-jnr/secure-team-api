package com.chatokjunior.secureteamapi.user;

import com.chatokjunior.secureteamapi.user.dto.MyProfileResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public MyProfileResponse myProfile(Authentication authentication){
        return userService.myProfile(authentication);
    }
}
