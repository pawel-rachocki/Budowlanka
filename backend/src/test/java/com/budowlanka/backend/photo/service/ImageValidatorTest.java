package com.budowlanka.backend.photo.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budowlanka.backend.photo.exception.InvalidImageException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageValidatorTest {

  private final ImageValidator validator = new ImageValidator();

  // --- helpers ---

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

  /** JPEG magic bytes + padding to exceed 5 MB — MIME passes, size fails. */
  private byte[] oversizedJpegHeader() {
    byte[] bytes = new byte[5 * 1024 * 1024 + 1];
    bytes[0] = (byte) 0xFF;
    bytes[1] = (byte) 0xD8;
    bytes[2] = (byte) 0xFF;
    return bytes;
  }

  // --- MIME ---

  @Test
  void should_throwInvalidImageException_when_gifProvided() {
    byte[] gif = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61};

    assertThatThrownBy(() -> validator.validate(gif))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("format");
  }

  @Test
  void should_throwInvalidImageException_when_bmpProvided() {
    byte[] bmp = {0x42, 0x4D, 0x00, 0x00};

    assertThatThrownBy(() -> validator.validate(bmp))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("format");
  }

  // --- File size ---

  @Test
  void should_throwInvalidImageException_when_fileTooLarge() {
    assertThatThrownBy(() -> validator.validate(oversizedJpegHeader()))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("5 MB");
  }

  // --- Dimensions ---

  @Test
  void should_throwInvalidImageException_when_imageTooSmall() throws IOException {
    assertThatThrownBy(() -> validator.validate(jpeg(100, 100)))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("200x200");
  }

  @Test
  void should_throwInvalidImageException_when_imageTooWide() throws IOException {
    assertThatThrownBy(() -> validator.validate(jpeg(4001, 300)))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("4000x4000");
  }

  @Test
  void should_throwInvalidImageException_when_imageTooTall() throws IOException {
    assertThatThrownBy(() -> validator.validate(jpeg(300, 4001)))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("4000x4000");
  }

  // --- Edge cases ---

  @Test
  void should_throwInvalidImageException_when_nullBytes() {
    assertThatThrownBy(() -> validator.validate(null))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("pusty");
  }

  @Test
  void should_throwInvalidImageException_when_emptyBytes() {
    assertThatThrownBy(() -> validator.validate(new byte[0]))
        .isInstanceOf(InvalidImageException.class)
        .hasMessageContaining("pusty");
  }

  // --- Happy paths ---

  @Test
  void should_pass_when_exactMinimumDimensions() throws IOException {
    assertThatCode(() -> validator.validate(jpeg(200, 200))).doesNotThrowAnyException();
  }

  @Test
  void should_pass_when_validJpeg() throws IOException {
    assertThatCode(() -> validator.validate(jpeg(800, 600))).doesNotThrowAnyException();
  }

  @Test
  void should_pass_when_validPng() throws IOException {
    assertThatCode(() -> validator.validate(png(800, 600))).doesNotThrowAnyException();
  }
}
