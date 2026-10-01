package com.chatokjunior.secureteamapi.auth.dto;

import lombok.Getter;

@Getter
public class CreateUserRequest {
    private String fullName;
    private String email;
    private String password;
}
