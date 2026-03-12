package com.budowlanka.backend.auth.controller;

import com.budowlanka.backend.auth.dto.LoginRequest;
import com.budowlanka.backend.auth.dto.LoginResponse;
import com.budowlanka.backend.auth.dto.MessageResponse;
import com.budowlanka.backend.auth.dto.RegisterRequest;
import com.budowlanka.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return new MessageResponse("Rejestracja udana. Sprawdź email, aby aktywować konto.");
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @GetMapping("/verify")
  public ResponseEntity<MessageResponse> verify(@RequestParam @Size(max = 128) String token) {
    authService.verifyEmail(token);
    return ResponseEntity.ok(new MessageResponse("Email zweryfikowany. Możesz się zalogować."));
  }
}
