package com.budowlanka.backend.photo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budowlanka.backend.config.S3Config;
import com.budowlanka.backend.config.S3Properties;
import com.budowlanka.backend.photo.enums.StorageKeySuffix;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Testcontainers
class S3StorageServiceIT {

  @Container
  static GenericContainer<?> minio =
      new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2024-05-10T01-41-38Z"))
          .withCommand("server /data --console-address :9001")
          .withEnv("MINIO_ROOT_USER", "minioadmin")
          .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
          .withExposedPorts(9000)
          .waitingFor(Wait.forHttp("/minio/health/live").forPort(9000));

  private static S3StorageService storageService;
  private static S3Client s3Client;
  private static S3Properties props;

  @BeforeAll
  static void setUp() {
    int port = minio.getMappedPort(9000);
    props =
        new S3Properties(
            "http://localhost:" + port,
            "us-east-1",
            "test-photos",
            "minioadmin",
            "minioadmin",
            true,
            "http://localhost:" + port + "/test-photos");
    s3Client = new S3Config(props).s3Client();
    storageService = new S3StorageService(s3Client, props);
  }

  @AfterAll
  static void tearDown() {
    if (s3Client != null) s3Client.close();
  }

  @Test
  void should_downloadMatchingContent_when_objectUploaded() {
    byte[] content = "hello-world".getBytes(StandardCharsets.UTF_8);
    String key = storageService.buildKey(UUID.randomUUID(), StorageKeySuffix.ORIGINAL);

    storageService.uploadObject(content, key, "image/jpeg");

    byte[] downloaded =
        s3Client
            .getObjectAsBytes(GetObjectRequest.builder().bucket(props.bucket()).key(key).build())
            .asByteArray();
    assertThat(downloaded).isEqualTo(content);
  }

  @Test
  void should_throwNoSuchKeyException_when_objectDeleted() {
    byte[] content = "to-delete".getBytes(StandardCharsets.UTF_8);
    String key = storageService.buildKey(UUID.randomUUID(), StorageKeySuffix.ORIGINAL);
    storageService.uploadObject(content, key, "image/jpeg");

    storageService.deleteObject(key);

    assertThatThrownBy(
            () ->
                s3Client.getObject(
                    GetObjectRequest.builder().bucket(props.bucket()).key(key).build()))
        .isInstanceOf(NoSuchKeyException.class);
  }
}
