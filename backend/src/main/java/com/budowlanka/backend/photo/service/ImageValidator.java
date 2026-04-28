package com.budowlanka.backend.photo.service;

import com.budowlanka.backend.photo.exception.InvalidImageException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Component;

@Component
public class ImageValidator {

  private static final long MAX_BYTES = 5L * 1024 * 1024;
  private static final int MIN_DIM = 200;
  private static final int MAX_DIM = 4000;

  public void validate(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      throw new InvalidImageException("Plik jest pusty.");
    }
    checkMimeType(bytes);
    checkFileSize(bytes);
    checkDimensions(bytes);
  }

  private void checkMimeType(byte[] bytes) {
    if (isJpeg(bytes) || isPng(bytes)) {
      return;
    }
    throw new InvalidImageException("Niedozwolony format pliku. Akceptowane: JPEG, PNG.");
  }

  private void checkFileSize(byte[] bytes) {
    if (bytes.length > MAX_BYTES) {
      throw new InvalidImageException("Plik jest zbyt duży. Maksymalny rozmiar: 5 MB.");
    }
  }

  /**
   * Reads only the image header via ImageReader — no full pixel decode, so a compressed-but-large
   * image cannot trigger excessive memory allocation.
   */
  private void checkDimensions(byte[] bytes) {
    try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      if (iis == null) {
        throw new InvalidImageException("Nie można odczytać obrazu.");
      }
      Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
      if (!readers.hasNext()) {
        throw new InvalidImageException("Nie można odczytać obrazu.");
      }
      ImageReader reader = readers.next();
      try {
        reader.setInput(iis, true, true);
        int w = reader.getWidth(0);
        int h = reader.getHeight(0);
        if (w < MIN_DIM || h < MIN_DIM) {
          throw new InvalidImageException(
              "Obraz jest zbyt mały. Minimalne wymiary: " + MIN_DIM + "x" + MIN_DIM + " px.");
        }
        if (w > MAX_DIM || h > MAX_DIM) {
          throw new InvalidImageException(
              "Obraz jest zbyt duży. Maksymalne wymiary: " + MAX_DIM + "x" + MAX_DIM + " px.");
        }
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new InvalidImageException("Nie można odczytać obrazu.");
    }
  }

  private boolean isJpeg(byte[] bytes) {
    return bytes.length >= 3
        && (bytes[0] & 0xFF) == 0xFF
        && (bytes[1] & 0xFF) == 0xD8
        && (bytes[2] & 0xFF) == 0xFF;
  }

  private boolean isPng(byte[] bytes) {
    return bytes.length >= 4
        && (bytes[0] & 0xFF) == 0x89
        && (bytes[1] & 0xFF) == 0x50
        && (bytes[2] & 0xFF) == 0x4E
        && (bytes[3] & 0xFF) == 0x47;
  }
}
