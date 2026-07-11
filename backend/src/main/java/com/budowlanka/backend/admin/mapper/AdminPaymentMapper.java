package com.budowlanka.backend.admin.mapper;

import com.budowlanka.backend.admin.dto.AdminPaymentResponse;
import com.budowlanka.backend.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminPaymentMapper {

  @Mapping(source = "crewProfile.companyName", target = "crewCompanyName")
  AdminPaymentResponse toResponse(Payment payment);
}
