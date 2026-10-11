package com.chatokjunior.secureteamapi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank
        String token,

        @NotBlank
        @Size(min=8, max=20, message = "Password length must be between 8 to 20")
        String newPassword
) {
}
