package com.micarch.tracing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TracingControllerTest {
    @Test
    void describesTracePropagation() {
        assertThat(new TracingController().checkout()).containsEntry("exporter", "Zipkin");
    }
}
