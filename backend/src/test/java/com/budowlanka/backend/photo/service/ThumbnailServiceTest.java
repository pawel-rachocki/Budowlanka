package com.budowlanka.backend.photo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ThumbnailServiceTest {

  private static final double ASPECT_RATIO_TOLERANCE = 0.02;

  private final ThumbnailService service = new ThumbnailService();

  private byte[] jpeg(int w, int h) throws IOException {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", out);
    return out.toByteArray();
  }

  private byte[] png(int w, int h) throws IOException {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(img, "png", out);
    return out.toByteArray();
  }

  private BufferedImage read(byte[] bytes) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(bytes));
  }

  @Test
  void should_generateThumbnail_when_landscapeImage() throws IOException {
    byte[] thumbnail = service.generate(jpeg(1920, 1080));

    BufferedImage result = read(thumbnail);
    assertThat(result.getWidth()).isLessThanOrEqualTo(400);
    assertThat(result.getHeight()).isLessThanOrEqualTo(400);
  }

  @Test
  void should_generateThumbnail_when_portraitImage() throws IOException {
    byte[] thumbnail = service.generate(jpeg(800, 1200));

    BufferedImage result = read(thumbnail);
    assertThat(result.getWidth()).isLessThanOrEqualTo(400);
    assertThat(result.getHeight()).isLessThanOrEqualTo(400);
  }

  @Test
  void should_keepAspectRatio_when_landscapeImage() throws IOException {
    byte[] thumbnail = service.generate(jpeg(1920, 1080));

    BufferedImage result = read(thumbnail);
    double originalRatio = 1920.0 / 1080.0;
    double thumbRatio = (double) result.getWidth() / result.getHeight();
    assertThat(thumbRatio).isCloseTo(originalRatio, offset(ASPECT_RATIO_TOLERANCE));
  }

  @Test
  void should_notUpscale_when_imageSmallerThanMaxSide() throws IOException {
    byte[] thumbnail = service.generate(jpeg(200, 200));

    BufferedImage result = read(thumbnail);
    assertThat(result.getWidth()).isEqualTo(200);
    assertThat(result.getHeight()).isEqualTo(200);
  }

  @Test
  void should_outputJpeg_when_pngInputProvided() throws IOException {
    byte[] thumbnail = service.generate(png(800, 600));

    assertThat(thumbnail[0] & 0xFF).isEqualTo(0xFF);
    assertThat(thumbnail[1] & 0xFF).isEqualTo(0xD8);
    assertThat(thumbnail[2] & 0xFF).isEqualTo(0xFF);
  }
}
