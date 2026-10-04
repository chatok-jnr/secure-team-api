package com.chatokjunior.secureteamapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {
    @NotBlank
    @NotEmpty
    public String currentPassword;

    @NotBlank
    @Size(min=8, max=20, message = "Password length must be between 8 to 20")
    public String newPassword;
}
