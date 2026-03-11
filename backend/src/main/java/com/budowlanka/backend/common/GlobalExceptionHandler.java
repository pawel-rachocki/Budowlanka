package com.budowlanka.backend.common;

import com.budowlanka.backend.auth.exception.DuplicateEmailException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

  @ExceptionHandler(DuplicateEmailException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError handleDuplicateEmail(DuplicateEmailException ex) {
    log.warn("Duplicate email registration attempt: {}", ex.getMessage());
    return ApiError.conflict("Podany adres email jest już zarejestrowany.");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleIllegalArgument(IllegalArgumentException ex) {
    return ApiError.of(400, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    return ApiError.of(500, "Wewnętrzny błąd serwera.");
  }
}
