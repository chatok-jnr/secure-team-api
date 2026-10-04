package com.chatokjunior.secureteamapi.exception;

public class PasswordMismatchedException extends RuntimeException {
  public PasswordMismatchedException(String message) {
    super(message);
  }
}
