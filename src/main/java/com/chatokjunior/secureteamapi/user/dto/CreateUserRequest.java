package com.chatokjunior.secureteamapi.user.dto;

import com.chatokjunior.secureteamapi.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    @Email(message = "Please enter an valid email")
    private String email;

    @NotBlank
    @Size(min = 3, max = 30)
    private String fullName;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
    
    private Role role;
}
