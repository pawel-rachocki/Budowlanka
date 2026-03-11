package com.budowlanka.backend.auth.controller;

import com.budowlanka.backend.auth.dto.MessageResponse;
import com.budowlanka.backend.auth.dto.RegisterRequest;
import com.budowlanka.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
