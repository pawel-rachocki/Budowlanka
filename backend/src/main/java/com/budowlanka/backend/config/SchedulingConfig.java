package com.budowlanka.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Włącza obsługę {@code @Scheduled} w aplikacji. Wydzielone z {@code AsyncConfig}, by rozdzielić
 * odpowiedzialności (async task executor vs. cykliczne joby).
 *
 * <p>Zakładamy pojedynczą instancję backendu (MVP) — cron odpala się w każdej JVM osobno. Przy
 * przejściu na wiele instancji należy dołożyć distributed lock (np. ShedLock), by uniknąć
 * równoległych wykonań; logika jobów jest już idempotentna, więc byłaby to zmiana punktowa.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
