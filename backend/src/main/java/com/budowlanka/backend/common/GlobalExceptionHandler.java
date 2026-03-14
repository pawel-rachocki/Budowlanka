package com.budowlanka.backend.common;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleValidation(MethodArgumentNotValidException ex) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
    return ApiError.validationError(errors);
  }

  @ExceptionHandler(DisabledException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiError handleDisabled(DisabledException ex) {
    return ApiError.of(401, "Email niezweryfikowany. Sprawdź skrzynkę pocztową.");
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiError handleBadCredentials(BadCredentialsException ex) {
    return ApiError.of(401, "Nieprawidłowy email lub hasło.");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return ApiError.of(400, "Nieprawidłowe żądanie.");
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
    int status = ex.getStatusCode().value();
    if (status >= 500) {
      log.error("ResponseStatusException: {}", ex.getReason(), ex);
    } else if (status == 401 || status == 403) {
      log.debug("Auth failure ({}): {}", status, ex.getReason());
    }
    String message = ex.getReason() != null ? ex.getReason() : "Błąd żądania.";
    return ResponseEntity.status(status).body(ApiError.of(status, message));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    return ApiError.of(500, "Wewnętrzny błąd serwera.");
  }
}
