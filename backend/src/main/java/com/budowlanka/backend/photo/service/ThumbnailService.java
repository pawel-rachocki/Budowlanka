package com.budowlanka.backend.photo.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

@Service
public class ThumbnailService {

  private static final int MAX_SIDE = 400;

  public byte[] generate(byte[] originalBytes) throws IOException {
    BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalBytes));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(original);
    if (original.getWidth() > MAX_SIDE || original.getHeight() > MAX_SIDE) {
      builder.size(MAX_SIDE, MAX_SIDE).keepAspectRatio(true);
    } else {
      builder.scale(1.0);
    }
    builder.outputFormat("jpg").toOutputStream(out);
    return out.toByteArray();
  }
}
