package com.micarch.retry;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class RetryControllerTest {
    @Test
    void successfulCallReturnsValue() throws Exception {
        var controller = new RetryController();
        assertThat(controller.fetch("one").get()).isEqualTo("value:one");
    }

    @Test
    void failedFutureIsObservableBeforeFrameworkFallback() {
        var controller = new RetryController();
        var future = controller.fetch("fail-one");
        assertThat(future).isCompletedExceptionally();
    }
}
