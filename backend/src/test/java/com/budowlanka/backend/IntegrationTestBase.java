package com.budowlanka.backend;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;

public abstract class IntegrationTestBase {

  @MockitoBean protected S3Client s3Client;
}
