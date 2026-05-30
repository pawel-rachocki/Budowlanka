package com.budowlanka.backend.review.mapper;

import com.budowlanka.backend.review.dto.ReviewResponse;
import com.budowlanka.backend.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mapping(
      target = "authorDisplayName",
      expression = "java(maskEmail(review.getAuthor().getEmail()))")
  ReviewResponse toResponse(Review review);

  @Named("maskEmail")
  default String maskEmail(String email) {
    int at = email.indexOf('@');
    if (at <= 0) return "Użytkownik";
    String local = email.substring(0, at);
    return local.substring(0, Math.min(3, local.length())) + "***";
  }
}
