package com.budowlanka.backend.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.exception.AdminRegistrationException;
import com.budowlanka.backend.auth.exception.EmailAlreadyExistsException;
import com.budowlanka.backend.auth.exception.InvalidTokenException;
import com.budowlanka.backend.auth.exception.VerificationTokenException;
import com.budowlanka.backend.crew.exception.BlankFieldException;
import com.budowlanka.backend.crew.exception.ServiceCategoryNotFoundException;
import com.budowlanka.backend.payment.exception.P24ClientException;
import com.budowlanka.backend.payment.exception.PackageNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void should_return400WithFieldErrors_when_validationFails() {
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    when(ex.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors())
        .thenReturn(List.of(new FieldError("req", "email", "must not be blank")));

    ApiError result = handler.handleValidation(ex);

    assertThat(result.status()).isEqualTo(400);
    assertThat(result.message()).isEqualTo("Validation failed");
    assertThat(result.errors()).containsExactly("email: must not be blank");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return400_when_httpMessageNotReadable() {
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

    ApiError result = handler.handleNotReadable(ex);

    assertThat(result.status()).isEqualTo(400);
    assertThat(result.message()).isEqualTo("Nieprawidłowy format danych żądania.");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return403WithUnverifiedMessage_when_disabledException() {
    ApiError result = handler.handleDisabled(new DisabledException("test"));

    assertThat(result.status()).isEqualTo(403);
    assertThat(result.message()).isEqualTo("Email niezweryfikowany. Sprawdź skrzynkę pocztową.");
  }

  @Test
  void should_return401WithCredentialsMessage_when_badCredentialsException() {
    ApiError result = handler.handleBadCredentials(new BadCredentialsException("test"));

    assertThat(result.status()).isEqualTo(401);
    assertThat(result.message()).isEqualTo("Nieprawidłowy email lub hasło.");
  }

  @Test
  void should_return400WithOriginalMessage_when_illegalArgumentException() {
    ApiError result =
        handler.handleIllegalArgument(
            new IllegalArgumentException("Pole companyName nie może być puste."));

    assertThat(result.status()).isEqualTo(400);
    assertThat(result.message()).isEqualTo("Pole companyName nie może być puste.");
  }

  @Test
  void should_returnCustomStatusAndReason_when_responseStatusException() {
    ResponseStatusException ex =
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Zasób nie istnieje.");

    ResponseEntity<ApiError> result = handler.handleResponseStatus(ex);

    assertThat(result.getStatusCode().value()).isEqualTo(404);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().status()).isEqualTo(404);
    assertThat(result.getBody().message()).isEqualTo("Zasób nie istnieje.");
  }

  @Test
  void should_returnFallbackMessage_when_responseStatusExceptionHasNoReason() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT);

    ResponseEntity<ApiError> result = handler.handleResponseStatus(ex);

    assertThat(result.getStatusCode().value()).isEqualTo(409);
    assertThat(result.getBody().message()).isEqualTo("Błąd żądania.");
  }

  @Test
  void should_return409WithEmailTakenMessage_when_emailAlreadyExists() {
    ApiError result = handler.handleEmailAlreadyExists(new EmailAlreadyExistsException());

    assertThat(result.status()).isEqualTo(409);
    assertThat(result.message()).isEqualTo("Email jest już zajęty.");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return401_when_invalidTokenException() {
    ApiError result =
        handler.handleInvalidToken(
            new InvalidTokenException("Sesja wygasła. Zaloguj się ponownie."));

    assertThat(result.status()).isEqualTo(401);
    assertThat(result.message()).isEqualTo("Sesja wygasła. Zaloguj się ponownie.");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return400_when_verificationTokenException() {
    ApiError result =
        handler.handleVerificationToken(
            new VerificationTokenException("Token weryfikacyjny wygasł."));

    assertThat(result.status()).isEqualTo(400);
    assertThat(result.message()).isEqualTo("Token weryfikacyjny wygasł.");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return403_when_adminRegistrationException() {
    ApiError result = handler.handleAdminRegistration(new AdminRegistrationException());

    assertThat(result.status()).isEqualTo(403);
    assertThat(result.message()).contains("ADMIN");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return400_when_serviceCategoryNotFoundException() {
    ApiError result = handler.handleServiceCategoryNotFound(new ServiceCategoryNotFoundException());

    assertThat(result.status()).isEqualTo(400);
    assertThat(result.message()).contains("kategorii");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return400_when_blankFieldException() {
    ApiError result = handler.handleBlankField(new BlankFieldException("companyName"));

    assertThat(result.status()).isEqualTo(400);
    assertThat(result.message()).isEqualTo("Pole companyName nie może być puste.");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return404_when_packageNotFoundException() {
    ApiError result = handler.handlePackageNotFound(new PackageNotFoundException());

    assertThat(result.status()).isEqualTo(404);
    assertThat(result.message()).isEqualTo("Pakiet nie istnieje lub jest nieaktywny.");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return502WithGatewayMessage_when_p24ClientException() {
    ApiError result =
        handler.handleP24Client(new P24ClientException("Błąd komunikacji z Przelewy24 (register)"));

    assertThat(result.status()).isEqualTo(502);
    assertThat(result.message())
        .isEqualTo("Błąd komunikacji z operatorem płatności. Spróbuj ponownie później.");
    assertThat(result.timestamp()).isNotNull();
  }

  @Test
  void should_return500WithGenericMessage_when_unhandledException() {
    ApiError result = handler.handleGeneric(new RuntimeException("unexpected"));

    assertThat(result.status()).isEqualTo(500);
    assertThat(result.message()).isEqualTo("Wewnętrzny błąd serwera.");
  }
}
