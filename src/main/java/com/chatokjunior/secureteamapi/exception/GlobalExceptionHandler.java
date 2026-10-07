package com.chatokjunior.secureteamapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import tools.jackson.databind.exc.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Validation Failed");

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(ProjectMemberAlreadyExistsException.class)
    public ProblemDetail handleProjectMemberAlreadyExistException(
            ProjectMemberAlreadyExistsException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Project Member Already Exist");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ProblemDetail handleProjectNotFoundException(
        ProjectNotFoundException ex
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Project Not Found");

        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbiddenException(
            ForbiddenException ex
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");

        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(NotAProjectMemberException.class)
    public ProblemDetail handleForbiddenException(
            NotAProjectMemberException ex
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Not A Project Member");

        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(ProjectMemberNotFoundException.class)
    public ProblemDetail handleForbiddenException(
            ProjectMemberNotFoundException ex
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Project Member Not Found");

        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(
            UserNotFoundException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("User Not Found");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExist(
            UserAlreadyExistsException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("User Already Exists");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLockedException(
            LockedException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.LOCKED);

        problem.setTitle("Account Is Locked");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(
            AuthenticationException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Authentication Failed");
        problem.setDetail("Invalid Email or Password");

        return problem;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshTokenException(
            InvalidRefreshTokenException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Invalid Refresh Token");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(PasswordMismatchedException.class)
    public ProblemDetail handlePasswordMismatchedException(
            PasswordMismatchedException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Incorrect Password");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(AccountLockedException.class)
    public ProblemDetail handleAccountLockedException(
            AccountLockedException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Account Locked");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialException(
            BadCredentialsException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Authentication Failed");
        problem.setDetail("Invalid Email or Password");

        return problem;
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDenied(
            AuthorizationDeniedException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.FORBIDDEN);

        problem.setTitle("Forbidden");
        problem.setDetail(
                "You do not have permission to perform this action"
        );

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid Request");

        String detail = "Request body contains an invalid value";

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {

            String field = invalidFormatException
                    .getPath()
                    .getLast()
                    .getPropertyName();

            Object value = invalidFormatException.getValue();

            detail = "Value '" + value
                    + "' is invalid for field '"
                    + field + "'";
        }

        problem.setDetail(detail);

        return problem;
    }
}