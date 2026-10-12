package com.chatokjunior.secureteamapi.exception;

public class OtpAlreadyExistException extends RuntimeException {
    public OtpAlreadyExistException(String message) {
        super(message);
    }
}
