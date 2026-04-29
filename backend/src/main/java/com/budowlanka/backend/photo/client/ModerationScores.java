package com.budowlanka.backend.photo.client;

public record ModerationScores(double nudity, double gore, double weapon, double drugs) {

  public static ModerationScores clean() {
    return new ModerationScores(0.0, 0.0, 0.0, 0.0);
  }
}
