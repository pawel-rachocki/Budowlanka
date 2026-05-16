package com.budowlanka.backend.photo.service;

import com.budowlanka.backend.config.S3Properties;
import com.budowlanka.backend.photo.enums.StorageKeySuffix;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class S3StorageService {

  private final S3Client s3Client;
  private final S3Properties props;

  public S3StorageService(S3Client s3Client, S3Properties props) {
    this.s3Client = s3Client;
    this.props = props;
  }

  public String uploadObject(byte[] bytes, String key, String contentType) {
    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(props.bucket())
              .key(key)
              .contentType(contentType)
              .contentLength((long) bytes.length)
              .build(),
          RequestBody.fromBytes(bytes));
      return key;
    } catch (SdkException e) {
      log.error("S3 upload failed, key={}", key, e);
      throw e;
    }
  }

  public void deleteObject(String key) {
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder().bucket(props.bucket()).key(key).build());
    } catch (SdkException e) {
      log.error("S3 delete failed, key={}", key, e);
      throw e;
    }
  }

  public String buildKey(UUID crewId, StorageKeySuffix suffix) {
    return "crew/" + crewId + "/" + UUID.randomUUID() + "-" + suffix.value() + ".jpg";
  }

  public String publicUrl(String key) {
    String base = props.publicBaseUrl();
    if (base == null || base.isBlank()) {
      return key;
    }
    return base.endsWith("/") ? base + key : base + "/" + key;
  }
}
