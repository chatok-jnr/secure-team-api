package com.chatokjunior.secureteamapi.exception;

public class EmailIsNotVerifiedException extends RuntimeException {
    public EmailIsNotVerifiedException(String message) {
        super(message);
    }
}
