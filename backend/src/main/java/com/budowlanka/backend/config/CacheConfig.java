package com.budowlanka.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    // Dynamic mode — cache'e tworzone on-demand na podstawie nazw z @Cacheable.
    // Brak TTL; dziś wystarcza (kategorie są seedowane przez Flyway i nie mutują).
    // Gdy pojawi się potrzeba wygasania wpisów — podmiana na Caffeine.
    return new ConcurrentMapCacheManager();
  }
}
