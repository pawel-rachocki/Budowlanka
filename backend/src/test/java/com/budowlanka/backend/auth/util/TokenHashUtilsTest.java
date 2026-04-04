package com.budowlanka.backend.auth.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHashUtilsTest {

  @Test
  void should_returnDifferentValue_than_input() {
    String input = "mojtoken123";

    String result = TokenHashUtils.hash(input);

    assertThat(result).isNotEqualTo(input);
  }

  @Test
  void should_returnSameHash_for_sameInput() {
    String input = "mojtoken123";

    String firstCall = TokenHashUtils.hash(input);
    String secondCall = TokenHashUtils.hash(input);

    assertThat(firstCall).isEqualTo(secondCall);
  }

  @Test
  void should_returnDifferentHash_for_differentInputs() {
    String hash1 = TokenHashUtils.hash("token-aaa");
    String hash2 = TokenHashUtils.hash("token-bbb");

    assertThat(hash1).isNotEqualTo(hash2);
  }
}
