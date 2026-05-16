package com.budowlanka.backend.crew.mapper;

import com.budowlanka.backend.crew.dto.CrewProfileResponse;
import com.budowlanka.backend.crew.dto.CrewProfileSummaryResponse;
import com.budowlanka.backend.crew.dto.ServiceCategoryResponse;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.entity.ServiceCategory;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CrewProfileMapper {

  @Mapping(target = "voivodeship", expression = "java(profile.getVoivodeship().name())")
  @Mapping(target = "serviceCategories", qualifiedByName = "sortedCategories")
  CrewProfileResponse toResponse(CrewProfile profile);

  @Mapping(target = "voivodeship", expression = "java(profile.getVoivodeship().name())")
  @Mapping(target = "phone", ignore = true)
  @Mapping(target = "contactEmail", ignore = true)
  @Mapping(target = "serviceCategories", qualifiedByName = "sortedCategories")
  CrewProfileResponse toResponsePublic(CrewProfile profile);

  @Mapping(target = "voivodeship", expression = "java(profile.getVoivodeship().name())")
  @Mapping(target = "serviceCategories", qualifiedByName = "sortedCategories")
  CrewProfileSummaryResponse toSummaryResponse(CrewProfile profile);

  @Named("sortedCategories")
  default List<ServiceCategoryResponse> sortedCategories(Set<ServiceCategory> categories) {
    return categories.stream()
        .map(c -> new ServiceCategoryResponse(c.getId(), c.getName(), c.getSlug()))
        .sorted(Comparator.comparing(ServiceCategoryResponse::name))
        .toList();
  }
}
