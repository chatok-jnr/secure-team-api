package com.chatokjunior.secureteamapi.exception;

public class NotAProjectMemberException extends RuntimeException {
    public NotAProjectMemberException(String message) {
        super(message);
    }
}
