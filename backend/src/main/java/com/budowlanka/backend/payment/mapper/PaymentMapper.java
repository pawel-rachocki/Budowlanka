package com.budowlanka.backend.payment.mapper;

import com.budowlanka.backend.payment.dto.PaymentResponse;
import com.budowlanka.backend.payment.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentResponse toResponse(Payment payment);
}
