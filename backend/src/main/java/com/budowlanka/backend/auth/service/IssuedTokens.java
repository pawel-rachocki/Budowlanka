package com.budowlanka.backend.auth.service;

public record IssuedTokens(String accessToken, String plainRefreshToken) {}
