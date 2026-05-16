package com.budowlanka.backend.photo.controller;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.photo.dto.PhotoResponse;
import com.budowlanka.backend.photo.service.PhotoService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/crew")
@RequiredArgsConstructor
public class PhotoController {

  private final PhotoService photoService;

  @PostMapping(path = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("hasRole('CREW')")
  public PhotoResponse upload(
      @AuthenticationPrincipal User user,
      @RequestPart("file") MultipartFile file,
      @RequestParam(required = false) @Size(max = 255) String caption) {
    return photoService.upload(user.getId(), file, caption);
  }

  @GetMapping("/photos/me")
  @PreAuthorize("hasRole('CREW')")
  public List<PhotoResponse> listMine(@AuthenticationPrincipal User user) {
    return photoService.listMine(user.getId());
  }

  @DeleteMapping("/photos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('CREW')")
  public void delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
    photoService.delete(id, user.getId());
  }

  @GetMapping("/profiles/{slug}/photos")
  public List<PhotoResponse> listPublic(@PathVariable @NotBlank @Size(max = 255) String slug) {
    return photoService.listPublicBySlug(slug);
  }
}
