package com.budowlanka.backend.config;

import com.budowlanka.backend.common.filter.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

  /**
   * Disables servlet-container auto-registration of the {@code @Component} filter. The filter runs
   * only inside the Spring Security chain (after {@code CorsFilter}), so 429 responses carry CORS
   * headers readable by the frontend.
   */
  @Bean
  public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
      RateLimitFilter filter) {
    FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
