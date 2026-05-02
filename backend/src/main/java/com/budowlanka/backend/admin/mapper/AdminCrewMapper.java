package com.budowlanka.backend.admin.mapper;

import com.budowlanka.backend.admin.dto.AdminCrewResponse;
import com.budowlanka.backend.crew.entity.CrewProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminCrewMapper {

  @Mapping(target = "voivodeship", expression = "java(profile.getVoivodeship().name())")
  @Mapping(source = "user.email", target = "ownerEmail")
  AdminCrewResponse toResponse(CrewProfile profile);
}
