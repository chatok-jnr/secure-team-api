package com.chatokjunior.secureteamapi.auth.dto;

import lombok.Getter;

@Getter
public class LoginUserRequest {
    private String email;
    private String password;
}
