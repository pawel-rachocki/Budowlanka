package com.budowlanka.backend.photo.dto;

import jakarta.validation.constraints.Size;

public record PhotoUploadRequest(@Size(max = 255) String caption) {}
