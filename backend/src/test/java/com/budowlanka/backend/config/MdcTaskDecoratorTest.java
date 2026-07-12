package com.budowlanka.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class MdcTaskDecoratorTest {

  private final MdcTaskDecorator decorator = new MdcTaskDecorator();

  @AfterEach
  void cleanUpMdc() {
    MDC.clear();
  }

  @Test
  void should_copyCallerMdc_when_taskRuns() {
    MDC.put("requestId", "caller-request-id");
    AtomicReference<String> seenInTask = new AtomicReference<>();

    Runnable decorated = decorator.decorate(() -> seenInTask.set(MDC.get("requestId")));
    MDC.clear(); // simulates the task running later on a worker thread without the caller's MDC
    decorated.run();

    assertThat(seenInTask.get()).isEqualTo("caller-request-id");
  }

  @Test
  void should_restorePreviousMdc_when_taskCompletes() {
    MDC.put("requestId", "caller-request-id");
    Runnable decorated = decorator.decorate(() -> {});

    MDC.clear();
    MDC.put("requestId", "worker-own-id");
    decorated.run();

    assertThat(MDC.get("requestId")).isEqualTo("worker-own-id");
  }

  @Test
  void should_clearWorkerMdc_when_callerHadNoContext() {
    MDC.clear();
    AtomicReference<String> seenInTask = new AtomicReference<>();
    Runnable decorated = decorator.decorate(() -> seenInTask.set(MDC.get("requestId")));

    MDC.put("requestId", "stale-worker-id");
    decorated.run();

    assertThat(seenInTask.get()).isNull();
  }

  @Test
  void should_restorePreviousMdc_when_taskThrows() {
    MDC.put("requestId", "caller-request-id");
    Runnable decorated =
        decorator.decorate(
            () -> {
              throw new IllegalStateException("boom");
            });

    MDC.clear();
    MDC.put("requestId", "worker-own-id");
    try {
      decorated.run();
    } catch (IllegalStateException expected) {
      // exception propagates — worker MDC must still be restored
    }

    assertThat(MDC.get("requestId")).isEqualTo("worker-own-id");
  }
}
