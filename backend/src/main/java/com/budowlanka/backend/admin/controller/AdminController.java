package com.budowlanka.backend.admin.controller;

import com.budowlanka.backend.admin.dto.AdminCrewResponse;
import com.budowlanka.backend.admin.dto.AdminPaymentResponse;
import com.budowlanka.backend.admin.dto.AdminStatsResponse;
import com.budowlanka.backend.admin.dto.BlockCrewRequest;
import com.budowlanka.backend.admin.dto.ModerationDecisionRequest;
import com.budowlanka.backend.admin.dto.PhotoModerationItemResponse;
import com.budowlanka.backend.admin.dto.RevenuePointResponse;
import com.budowlanka.backend.admin.service.AdminCrewService;
import com.budowlanka.backend.admin.service.AdminModerationService;
import com.budowlanka.backend.admin.service.AdminPaymentService;
import com.budowlanka.backend.admin.service.AdminStatsService;
import com.budowlanka.backend.common.PagedResponse;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.photo.dto.PhotoResponse;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminController {

  private final AdminModerationService adminModerationService;
  private final AdminCrewService adminCrewService;
  private final AdminPaymentService adminPaymentService;
  private final AdminStatsService adminStatsService;

  @GetMapping("/stats")
  public AdminStatsResponse stats() {
    return adminStatsService.getStats();
  }

  @GetMapping("/stats/revenue")
  public List<RevenuePointResponse> revenueTimeline(
      @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
    return adminStatsService.getRevenueTimeline(days);
  }

  @GetMapping("/moderation/photos")
  public PagedResponse<PhotoModerationItemResponse> moderationQueue(
      @RequestParam(defaultValue = "PENDING") ModerationStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return PagedResponse.from(
        adminModerationService.queue(status, PageRequest.of(page, Math.min(size, 100))));
  }

  @PutMapping("/moderation/photos/{id}")
  public PhotoResponse decidePhoto(
      @PathVariable UUID id, @Valid @RequestBody ModerationDecisionRequest request) {
    return adminModerationService.decide(id, request);
  }

  @GetMapping("/crews")
  public PagedResponse<AdminCrewResponse> listCrews(
      @RequestParam(required = false) Boolean blocked,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
    return PagedResponse.from(adminCrewService.listCrews(blocked, pageable));
  }

  @PutMapping("/crews/{id}/block")
  public AdminCrewResponse blockCrew(
      @PathVariable UUID id, @Valid @RequestBody BlockCrewRequest request) {
    return adminCrewService.blockCrew(id, request);
  }

  @GetMapping("/payments")
  public PagedResponse<AdminPaymentResponse> listPayments(
      @RequestParam(required = false) PaymentStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
    return PagedResponse.from(adminPaymentService.listPayments(status, pageable));
  }
}
