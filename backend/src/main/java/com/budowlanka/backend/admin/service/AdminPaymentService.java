package com.budowlanka.backend.admin.service;

import com.budowlanka.backend.admin.dto.AdminPaymentResponse;
import com.budowlanka.backend.admin.mapper.AdminPaymentMapper;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPaymentService {

  private final PaymentRepository paymentRepository;
  private final AdminPaymentMapper adminPaymentMapper;

  @Transactional(readOnly = true)
  public Page<AdminPaymentResponse> listPayments(PaymentStatus status, Pageable pageable) {
    if (status != null) {
      return paymentRepository
          .findAllJoinCrewByStatus(status, pageable)
          .map(adminPaymentMapper::toResponse);
    }
    return paymentRepository.findAllJoinCrew(pageable).map(adminPaymentMapper::toResponse);
  }
}
