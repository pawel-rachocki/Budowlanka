package com.budowlanka.backend.payment.controller;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.payment.dto.SubscriptionStatusResponse;
import com.budowlanka.backend.payment.service.SubscriptionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Status subskrypcji i boosta zalogowanej ekipy — dane dla dashboardu ekipy (E-06). Logika w {@link
 * SubscriptionQueryService}; tu tylko warstwa HTTP.
 */
@RestController
@RequestMapping("/api/crew/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionQueryService subscriptionQueryService;

  @GetMapping("/me")
  @PreAuthorize("hasRole('CREW')")
  public SubscriptionStatusResponse mySubscription(@AuthenticationPrincipal User user) {
    return subscriptionQueryService.getStatus(user);
  }
}
