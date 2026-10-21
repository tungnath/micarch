package com.micarch.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogControllerTest {
    @Test
    void describesStructuredLogPipeline() {
        assertThat(new LogController().sample()).containsEntry("format", "JSON");
    }
}
