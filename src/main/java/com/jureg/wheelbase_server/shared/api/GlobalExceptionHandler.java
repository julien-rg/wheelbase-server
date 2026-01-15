package com.jureg.wheelbase_server.shared.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jureg.wheelbase_server.shared.api.exception.AccessDeniedException;
import com.jureg.wheelbase_server.shared.api.exception.AlreadyFollowingException;
import com.jureg.wheelbase_server.shared.api.exception.CannotFollowYourselfException;
import com.jureg.wheelbase_server.shared.api.exception.CannotUnfollowYourselfException;
import com.jureg.wheelbase_server.shared.api.exception.FieldAlreadyExistsException;
import com.jureg.wheelbase_server.shared.api.exception.InvalidCredentialsException;
import com.jureg.wheelbase_server.shared.api.exception.NotFollowingUserException;
import com.jureg.wheelbase_server.shared.api.exception.UserNotFoundException;

import jakarta.validation.UnexpectedTypeException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// -------------------------------------------------------------
	// Validation errors
	// -------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
        		.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));
    }
    
	// -------------------------------------------------------------
	// Type validation
	// -------------------------------------------------------------
    @ExceptionHandler(UnexpectedTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnexpectedType(UnexpectedTypeException ex) {
    	return Map.of(
            "error", "Invalid request",
            "parameter", ex.getMessage()
        );
    }
    
    // -------------------------------------------------------------
  	// Required parameters validation
  	// -------------------------------------------------------------
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMissingParams(MissingServletRequestParameterException ex) {
        return Map.of(
            "error", "Missing required parameter",
            "parameter", ex.getParameterName()
        );
    }
    
    // -------------------------------------------------------------
 	// Enums validation
 	// -------------------------------------------------------------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleInvalidEnum(HttpMessageNotReadableException ex) {

        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException ife
                && ife.getTargetType().isEnum()) {

            // Jackson 3: field name is not directly available
            String field = ife.getPath().isEmpty()
                    ? "unknown"
                    : ife.getPath().get(ife.getPath().size() - 1).toString();

            List<String> allowedValues =
                    Arrays.stream(ife.getTargetType().getEnumConstants())
                          .map(Object::toString)
                          .toList();

            return Map.of(
                "error", "Invalid enum value",
                "field", field,
                "allowedValues", allowedValues
            );
        }

        return Map.of(
            "error", "Invalid request body",
            "message", cause.getMessage()
        );
    }

	// -------------------------------------------------------------
	// Custom exceptions
	// -------------------------------------------------------------
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAccessDenied(AccessDeniedException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return Map.of("error", ex.getMessage());
    }
    
    @ExceptionHandler(CannotFollowYourselfException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleCannotFollowYourself(CannotFollowYourselfException ex) {
        return Map.of("error", ex.getMessage());
    }
    
    @ExceptionHandler(CannotUnfollowYourselfException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleCannotUnfollowYourself(CannotUnfollowYourselfException ex) {
        return Map.of("error", ex.getMessage());
    }
    
    @ExceptionHandler(NotFollowingUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNotFollowingUser(NotFollowingUserException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(AlreadyFollowingException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleAlreadyFollowing(AlreadyFollowingException ex) {
        return Map.of("error", ex.getMessage());
    }
    
    @ExceptionHandler(FieldAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleFieldAlreadyExists(FieldAlreadyExistsException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound(UserNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    // -------------------------------------------------------------
 	// Fallback exception
 	// -------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnexpected(Exception ex) {
        return Map.of("error", "Internal server error");
    }
}