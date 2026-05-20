package com.budowlanka.backend.review.mapper;

import com.budowlanka.backend.review.dto.ReviewResponse;
import com.budowlanka.backend.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mapping(source = "author.email", target = "authorDisplayName")
  ReviewResponse toResponse(Review review);
}
