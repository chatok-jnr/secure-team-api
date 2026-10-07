package com.chatokjunior.secureteamapi.exception;

public class ProjectMemberAlreadyExistsException extends RuntimeException {
  public ProjectMemberAlreadyExistsException(String message) {
    super(message);
  }
}
