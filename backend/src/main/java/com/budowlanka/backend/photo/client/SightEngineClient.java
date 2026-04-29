package com.budowlanka.backend.photo.client;

import com.budowlanka.backend.config.SightEngineProperties;
import com.budowlanka.backend.photo.exception.ModerationApiException;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class SightEngineClient {

  private final SightEngineProperties props;
  private final RestClient restClient;

  @Autowired
  public SightEngineClient(SightEngineProperties props) {
    this.props = props;
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(10));
    factory.setReadTimeout(Duration.ofSeconds(10));
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  SightEngineClient(SightEngineProperties props, RestClient restClient) {
    this.props = props;
    this.restClient = restClient;
  }

  public ModerationScores check(String imageUrl) {
    if (!props.enabled()) {
      return ModerationScores.clean();
    }
    try {
      URI uri =
          UriComponentsBuilder.fromUriString(props.baseUrl())
              .path("/1.0/check.json")
              .queryParam("models", "nudity,gore,weapon")
              .queryParam("url", imageUrl)
              .queryParam("api_user", props.apiUser())
              .queryParam("api_secret", props.apiSecret())
              .build()
              .toUri();

      JsonNode root = restClient.get().uri(uri).retrieve().body(JsonNode.class);
      return toScores(root);
    } catch (Exception e) {
      log.warn("SightEngine API error for url={}: {}", imageUrl, e.getMessage());
      throw new ModerationApiException("SightEngine API call failed", e);
    }
  }

  private ModerationScores toScores(JsonNode root) {
    double nudity = root.path("nudity").path("sexual_activity").asDouble(0.0);
    double gore = root.path("gore").path("prob").asDouble(0.0);
    double weapon = root.path("weapon").path("prob").asDouble(0.0);
    return new ModerationScores(nudity, gore, weapon, 0.0);
  }
}
