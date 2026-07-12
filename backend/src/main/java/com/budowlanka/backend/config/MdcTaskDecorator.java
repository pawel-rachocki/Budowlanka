package com.budowlanka.backend.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * Copies the caller's MDC (request id) into {@code @Async} worker threads so async work — e.g.
 * SightEngine photo moderation — stays correlated with the HTTP request that triggered it. Restores
 * the worker thread's previous MDC afterwards (threads are pooled).
 */
public class MdcTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    Map<String, String> callerContext = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> previousContext = MDC.getCopyOfContextMap();
      setContextMap(callerContext);
      try {
        runnable.run();
      } finally {
        setContextMap(previousContext);
      }
    };
  }

  private static void setContextMap(Map<String, String> contextMap) {
    if (contextMap != null) {
      MDC.setContextMap(contextMap);
    } else {
      MDC.clear();
    }
  }
}
