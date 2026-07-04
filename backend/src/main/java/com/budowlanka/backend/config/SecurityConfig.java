package com.budowlanka.backend.config;

import com.budowlanka.backend.auth.filter.JwtAuthFilter;
import com.budowlanka.backend.auth.service.UserDetailsServiceImpl;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;
  private final JwtAuthFilter jwtAuthFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(unauthorizedEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/auth/register")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth/verify")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/refresh")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/packages/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/crew/profiles")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/crew/profiles/me")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/crew/profiles/{slug}")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/crew/profiles")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/crew/profiles/me")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/crew/photos")
                    .hasRole("CREW")
                    .requestMatchers(HttpMethod.GET, "/api/crew/photos/me")
                    .hasRole("CREW")
                    .requestMatchers(HttpMethod.DELETE, "/api/crew/photos/{id}")
                    .hasRole("CREW")
                    .requestMatchers(HttpMethod.GET, "/api/crew/profiles/*/photos")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/crew/profiles/*/reviews")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/crew/profiles/*/reviews")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.PUT, "/api/crew/profiles/*/reviews/*")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.DELETE, "/api/crew/profiles/*/reviews/*")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.POST, "/api/payments/listing")
                    .hasRole("CREW")
                    .requestMatchers(HttpMethod.POST, "/api/payments/boost")
                    .hasRole("CREW")
                    .requestMatchers(HttpMethod.GET, "/api/payments/my")
                    .hasRole("CREW")
                    .requestMatchers(HttpMethod.POST, "/api/payments/webhook/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    return http.build();
  }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, ex) -> {
      response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      String body =
          String.format(
              "{\"status\":401,\"message\":\"Brak lub nieprawidłowy token autoryzacji.\",\"timestamp\":\"%s\"}",
              Instant.now());
      response.getWriter().write(body);
    };
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, ex) -> {
      response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      String body =
          String.format(
              "{\"status\":403,\"message\":\"Brak uprawnień do wykonania tej operacji.\",\"timestamp\":\"%s\"}",
              Instant.now());
      response.getWriter().write(body);
    };
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
