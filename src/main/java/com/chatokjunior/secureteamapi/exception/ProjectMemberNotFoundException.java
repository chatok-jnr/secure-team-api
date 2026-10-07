package com.chatokjunior.secureteamapi.exception;

public class ProjectMemberNotFoundException extends RuntimeException {
    public ProjectMemberNotFoundException(String message) {
        super(message);
    }
}
