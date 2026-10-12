package com.chatokjunior.secureteamapi.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class SessionResponse {
    private UUID id;
    private Instant createdAt;
    private Instant expiresAt;
}
