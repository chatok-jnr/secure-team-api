package com.chatokjunior.secureteamapi.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
  public InvalidPasswordResetTokenException(String message) {
    super(message);
  }
}
