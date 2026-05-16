package com.budowlanka.backend.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Configuration
public class S3Config {

  private final S3Properties props;

  public S3Config(S3Properties props) {
    this.props = props;
  }

  @Bean
  public S3Client s3Client() {
    var credentials =
        StaticCredentialsProvider.create(
            AwsBasicCredentials.create(props.accessKey(), props.secretKey()));

    boolean hasEndpoint = props.endpoint() != null && !props.endpoint().isBlank();
    boolean effectiveForcePathStyle = props.forcePathStyle() || hasEndpoint;

    var builder =
        S3Client.builder()
            .region(Region.of(props.region()))
            .credentialsProvider(credentials)
            .forcePathStyle(effectiveForcePathStyle);

    if (hasEndpoint) {
      builder.endpointOverride(URI.create(props.endpoint()));
    }

    S3Client client = builder.build();
    ensureBucketExists(client);
    return client;
  }

  private void ensureBucketExists(S3Client client) {
    String bucket = props.bucket();
    try {
      try {
        client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        log.info("S3 bucket '{}' already exists", bucket);
      } catch (NoSuchBucketException e) {
        client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        log.info("S3 bucket '{}' created", bucket);
      }
    } catch (S3Exception | SdkClientException e) {
      log.error("Failed to verify/create S3 bucket '{}': {}", bucket, e.getMessage());
      throw e;
    }
  }
}
