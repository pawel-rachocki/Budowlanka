package com.budowlanka.backend.photo.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.config.SightEngineProperties;
import com.budowlanka.backend.photo.exception.ModerationApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class SightEngineClientTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RestClient restClient;

  private static final SightEngineProperties DISABLED =
      new SightEngineProperties(false, "", "", "https://api.sightengine.com");

  private static final SightEngineProperties ENABLED =
      new SightEngineProperties(true, "user123", "secret123", "https://api.sightengine.com");

  @Test
  void should_return_clean_scores_when_moderation_disabled() {
    var client = new SightEngineClient(DISABLED);

    ModerationScores scores = client.check("https://example.com/photo.jpg");

    assertThat(scores.nudity()).isZero();
    assertThat(scores.gore()).isZero();
    assertThat(scores.weapon()).isZero();
    assertThat(scores.drugs()).isZero();
  }

  @Test
  void should_parse_sightengine_response_when_moderation_enabled() throws Exception {
    var client = new SightEngineClient(ENABLED, restClient);
    JsonNode mockResponse =
        new ObjectMapper()
            .readTree(
                """
                {
                  "status": "success",
                  "nudity": { "sexual_activity": 0.05 },
                  "gore":   { "prob": 0.02 },
                  "weapon": { "prob": 0.01 }
                }
                """);

    when(restClient.get().uri(any(URI.class)).retrieve().body(JsonNode.class))
        .thenReturn(mockResponse);

    ModerationScores scores = client.check("https://example.com/photo.jpg");

    assertThat(scores.nudity()).isEqualTo(0.05);
    assertThat(scores.gore()).isEqualTo(0.02);
    assertThat(scores.weapon()).isEqualTo(0.01);
    assertThat(scores.drugs()).isZero();
  }

  @Test
  void should_throw_moderation_api_exception_on_api_error() {
    var client = new SightEngineClient(ENABLED, restClient);

    when(restClient.get().uri(any(URI.class)).retrieve().body(JsonNode.class))
        .thenThrow(new RuntimeException("connection timeout"));

    assertThatThrownBy(() -> client.check("https://example.com/photo.jpg"))
        .isInstanceOf(ModerationApiException.class)
        .hasMessageContaining("SightEngine API call failed");
  }
}
