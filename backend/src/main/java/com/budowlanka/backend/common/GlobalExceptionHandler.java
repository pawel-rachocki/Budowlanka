package com.budowlanka.backend.common;

import com.budowlanka.backend.admin.exception.PhotoAlreadyDecidedException;
import com.budowlanka.backend.auth.exception.AdminRegistrationException;
import com.budowlanka.backend.auth.exception.EmailAlreadyExistsException;
import com.budowlanka.backend.auth.exception.InvalidTokenException;
import com.budowlanka.backend.auth.exception.VerificationTokenException;
import com.budowlanka.backend.crew.exception.BlankFieldException;
import com.budowlanka.backend.crew.exception.CrewProfileAlreadyExistsException;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.exception.ServiceCategoryNotFoundException;
import com.budowlanka.backend.photo.exception.InvalidImageException;
import com.budowlanka.backend.photo.exception.PhotoLimitExceededException;
import com.budowlanka.backend.photo.exception.PhotoNotFoundException;
import com.budowlanka.backend.photo.exception.PhotoOwnershipException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
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

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleConstraintViolation(ConstraintViolationException ex) {
    List<String> errors =
        ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .toList();
    return ApiError.validationError(errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String msg = "Nieprawidłowa wartość parametru '" + ex.getName() + "': " + ex.getValue();
    return ApiError.of(400, msg);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleNotReadable(HttpMessageNotReadableException ex) {
    log.warn("Malformed JSON request: {}", ex.getMessage());
    return ApiError.of(400, "Nieprawidłowy format danych żądania.");
  }

  @ExceptionHandler(DisabledException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiError handleDisabled(DisabledException ex) {
    return ApiError.of(403, "Email niezweryfikowany. Sprawdź skrzynkę pocztową.");
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiError handleBadCredentials(BadCredentialsException ex) {
    return ApiError.of(401, "Nieprawidłowy email lub hasło.");
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiError handleAccessDenied(AccessDeniedException ex) {
    log.debug("Access denied: {}", ex.getMessage());
    return ApiError.of(403, "Brak uprawnień do wykonania tej operacji.");
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    return ApiError.conflict(ex.getMessage());
  }

  @ExceptionHandler(CrewProfileAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError handleCrewProfileAlreadyExists(CrewProfileAlreadyExistsException ex) {
    return ApiError.conflict(ex.getMessage());
  }

  @ExceptionHandler(CrewProfileNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError handleCrewProfileNotFound(CrewProfileNotFoundException ex) {
    return ApiError.of(404, ex.getMessage());
  }

  @ExceptionHandler(InvalidTokenException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiError handleInvalidToken(InvalidTokenException ex) {
    log.debug("Invalid token: {}", ex.getMessage());
    return ApiError.unauthorized(ex.getMessage());
  }

  @ExceptionHandler(VerificationTokenException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleVerificationToken(VerificationTokenException ex) {
    return ApiError.badRequest(ex.getMessage());
  }

  @ExceptionHandler(AdminRegistrationException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiError handleAdminRegistration(AdminRegistrationException ex) {
    log.warn("Admin registration attempt blocked");
    return ApiError.of(403, ex.getMessage());
  }

  @ExceptionHandler(ServiceCategoryNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleServiceCategoryNotFound(ServiceCategoryNotFoundException ex) {
    return ApiError.badRequest(ex.getMessage());
  }

  @ExceptionHandler(BlankFieldException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleBlankField(BlankFieldException ex) {
    return ApiError.badRequest(ex.getMessage());
  }

  @ExceptionHandler(InvalidImageException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleInvalidImage(InvalidImageException ex) {
    return ApiError.badRequest(ex.getMessage());
  }

  @ExceptionHandler(PhotoNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError handlePhotoNotFound(PhotoNotFoundException ex) {
    return ApiError.of(404, ex.getMessage());
  }

  @ExceptionHandler(PhotoOwnershipException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiError handlePhotoOwnership(PhotoOwnershipException ex) {
    return ApiError.of(403, ex.getMessage());
  }

  @ExceptionHandler(PhotoLimitExceededException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ApiError handlePhotoLimitExceeded(PhotoLimitExceededException ex) {
    return ApiError.of(422, ex.getMessage());
  }

  @ExceptionHandler(PhotoAlreadyDecidedException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError handlePhotoAlreadyDecided(PhotoAlreadyDecidedException ex) {
    return ApiError.conflict(ex.getMessage());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  public ApiError handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
    return ApiError.of(415, "Nieobsługiwany typ zawartości: " + ex.getContentType());
  }

  @ExceptionHandler({
    MissingServletRequestPartException.class,
    MissingServletRequestParameterException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleMissingPart(Exception ex) {
    return ApiError.of(400, "Brakujący parametr żądania: " + ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return ApiError.of(400, ex.getMessage());
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
