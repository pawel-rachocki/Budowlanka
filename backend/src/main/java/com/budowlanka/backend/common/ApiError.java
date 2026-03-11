package com.budowlanka.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

public record ApiError(
    int status,
    String message,
    Instant timestamp,
    @JsonInclude(JsonInclude.Include.NON_NULL) List<String> errors) {

  public static ApiError validationError(List<String> errors) {
    return new ApiError(400, "Validation failed", Instant.now(), errors);
  }

  public static ApiError conflict(String message) {
    return new ApiError(409, message, Instant.now(), null);
  }

  public static ApiError of(int status, String message) {
    return new ApiError(status, message, Instant.now(), null);
  }
}
