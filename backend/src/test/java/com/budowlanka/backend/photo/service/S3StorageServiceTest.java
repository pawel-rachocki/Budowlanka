package com.budowlanka.backend.photo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.budowlanka.backend.config.S3Properties;
import com.budowlanka.backend.photo.enums.StorageKeySuffix;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

  @Mock private S3Client s3Client;

  private S3StorageService svc(String publicBaseUrl) {
    S3Properties props =
        new S3Properties("", "us-east-1", "bucket", "key", "secret", false, publicBaseUrl);
    return new S3StorageService(s3Client, props);
  }

  @Test
  void should_buildKey_with_correctFormat() {
    UUID crewId = UUID.randomUUID();

    String key = svc("https://cdn.example.com").buildKey(crewId, StorageKeySuffix.ORIGINAL);

    assertThat(key).matches("crew/" + crewId + "/[a-f0-9\\-]+-original\\.jpg");
  }

  @Test
  void should_buildKey_with_thumbSuffix() {
    UUID crewId = UUID.randomUUID();

    String key = svc("https://cdn.example.com").buildKey(crewId, StorageKeySuffix.THUMB);

    assertThat(key).matches("crew/" + crewId + "/[a-f0-9\\-]+-thumb\\.jpg");
  }

  @Test
  void should_buildPublicUrl_when_baseUrlHasNoTrailingSlash() {
    String url = svc("https://cdn.example.com/photos").publicUrl("crew/abc/123-original.jpg");

    assertThat(url).isEqualTo("https://cdn.example.com/photos/crew/abc/123-original.jpg");
  }

  @Test
  void should_buildPublicUrl_when_baseUrlHasTrailingSlash() {
    String url = svc("https://cdn.example.com/photos/").publicUrl("crew/abc/123-original.jpg");

    assertThat(url).isEqualTo("https://cdn.example.com/photos/crew/abc/123-original.jpg");
    assertThat(url).doesNotContain("//crew");
  }

  @Test
  void should_returnKey_when_publicBaseUrlIsNull() {
    String key = "crew/abc/123-original.jpg";

    assertThat(svc(null).publicUrl(key)).isEqualTo(key);
  }

  @Test
  void should_returnKey_when_publicBaseUrlIsBlank() {
    String key = "crew/abc/123-original.jpg";

    assertThat(svc("   ").publicUrl(key)).isEqualTo(key);
  }
}
