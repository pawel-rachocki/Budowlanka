package com.budowlanka.backend.auth.service;

import com.budowlanka.backend.auth.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
