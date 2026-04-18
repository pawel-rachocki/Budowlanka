package com.budowlanka.backend.auth.controller;

import com.budowlanka.backend.auth.dto.LoginRequest;
import com.budowlanka.backend.auth.dto.LoginResponse;
import com.budowlanka.backend.auth.dto.MessageResponse;
import com.budowlanka.backend.auth.dto.RefreshResponse;
import com.budowlanka.backend.auth.dto.RegisterRequest;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.exception.InvalidTokenException;
import com.budowlanka.backend.auth.service.AuthService;
import com.budowlanka.backend.auth.service.IssuedTokens;
import com.budowlanka.backend.auth.util.CookieUtils;
import com.budowlanka.backend.config.AppProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final AppProperties appProperties;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return new MessageResponse("Rejestracja udana. Sprawdź email, aby aktywować konto.");
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    IssuedTokens tokens = authService.login(request);
    long maxAgeSeconds = appProperties.jwt().refreshTokenExpiration() / 1000;
    CookieUtils.setRefreshCookie(
        response, tokens.plainRefreshToken(), maxAgeSeconds, appProperties.cookieSecure());
    return ResponseEntity.ok(new LoginResponse(tokens.accessToken(), "Bearer"));
  }

  @PostMapping("/refresh")
  public ResponseEntity<RefreshResponse> refresh(
      @CookieValue(name = CookieUtils.REFRESH_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new InvalidTokenException("Sesja wygasła. Zaloguj się ponownie.");
    }
    IssuedTokens tokens = authService.refreshToken(refreshToken);
    long maxAgeSeconds = appProperties.jwt().refreshTokenExpiration() / 1000;
    CookieUtils.setRefreshCookie(
        response, tokens.plainRefreshToken(), maxAgeSeconds, appProperties.cookieSecure());
    return ResponseEntity.ok(new RefreshResponse(tokens.accessToken()));
  }

  @GetMapping("/verify")
  public ResponseEntity<MessageResponse> verify(
      @RequestParam @NotBlank @Size(max = 128) String token) {
    authService.verifyEmail(token);
    return ResponseEntity.ok(new MessageResponse("Email zweryfikowany. Możesz się zalogować."));
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@AuthenticationPrincipal User user, HttpServletResponse response) {
    if (user == null) {
      throw new InvalidTokenException("Brak uwierzytelnienia.");
    }
    authService.logout(user);
    CookieUtils.clearRefreshCookie(response, appProperties.cookieSecure());
  }
}
