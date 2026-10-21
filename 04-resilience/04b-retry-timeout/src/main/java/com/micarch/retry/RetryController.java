package com.micarch.retry;

import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reliable")
public class RetryController {
    @GetMapping("/{key}")
    @Retry(name = "demoService", fallbackMethod = "fallback")
    @TimeLimiter(name = "demoService")
    public CompletableFuture<String> fetch(@PathVariable("key") String key) {
        if (key.startsWith("fail")) {
            return CompletableFuture.failedFuture(new IllegalStateException("transient failure"));
        }
        return CompletableFuture.completedFuture("value:" + key);
    }

    public CompletableFuture<String> fallback(String key, Throwable failure) {
        return CompletableFuture.completedFuture("fallback:" + key);
    }
}
