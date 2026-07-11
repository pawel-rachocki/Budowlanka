package com.budowlanka.backend.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.admin.dto.AdminPaymentResponse;
import com.budowlanka.backend.admin.mapper.AdminPaymentMapper;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.enums.PaymentType;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminPaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private AdminPaymentMapper adminPaymentMapper;

  private AdminPaymentService service;

  @BeforeEach
  void setUp() {
    service = new AdminPaymentService(paymentRepository, adminPaymentMapper);
    lenient()
        .when(adminPaymentMapper.toResponse(any(Payment.class)))
        .thenAnswer(
            inv -> {
              Payment p = inv.getArgument(0);
              return new AdminPaymentResponse(
                  p.getId(),
                  p.getCrewProfile() != null ? p.getCrewProfile().getCompanyName() : null,
                  p.getAmountPln(),
                  p.getPaymentType(),
                  p.getStatus(),
                  p.getProviderTxId(),
                  p.getCreatedAt(),
                  p.getCompletedAt());
            });
  }

  @Test
  void should_returnAllPayments_when_statusNull() {
    Pageable pageable = PageRequest.of(0, 20);
    Page<Payment> page = new PageImpl<>(List.of(payment()));
    when(paymentRepository.findAllJoinCrew(pageable)).thenReturn(page);

    Page<AdminPaymentResponse> result = service.listPayments(null, pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).crewCompanyName()).isEqualTo("Test Ekipa");
    verify(paymentRepository).findAllJoinCrew(pageable);
    verify(paymentRepository, never()).findAllJoinCrewByStatus(any(), any());
  }

  @Test
  void should_filterByStatus_when_statusProvided() {
    Pageable pageable = PageRequest.of(0, 20);
    Page<Payment> page = new PageImpl<>(List.of(payment()));
    when(paymentRepository.findAllJoinCrewByStatus(eq(PaymentStatus.COMPLETED), eq(pageable)))
        .thenReturn(page);

    Page<AdminPaymentResponse> result = service.listPayments(PaymentStatus.COMPLETED, pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(paymentRepository).findAllJoinCrewByStatus(PaymentStatus.COMPLETED, pageable);
    verify(paymentRepository, never()).findAllJoinCrew(any());
  }

  private Payment payment() {
    CrewProfile crew =
        CrewProfile.builder()
            .companyName("Test Ekipa")
            .slug("test-ekipa-warszawa")
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .build();
    return Payment.builder()
        .crewProfile(crew)
        .amountPln(new BigDecimal("89.00"))
        .paymentProvider("P24")
        .status(PaymentStatus.COMPLETED)
        .paymentType(PaymentType.LISTING)
        .build();
  }
}
