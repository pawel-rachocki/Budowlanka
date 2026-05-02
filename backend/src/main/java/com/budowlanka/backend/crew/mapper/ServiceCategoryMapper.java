package com.budowlanka.backend.crew.mapper;

import com.budowlanka.backend.crew.dto.ServiceCategoryResponse;
import com.budowlanka.backend.crew.entity.ServiceCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceCategoryMapper {
  ServiceCategoryResponse toResponse(ServiceCategory category);
}
