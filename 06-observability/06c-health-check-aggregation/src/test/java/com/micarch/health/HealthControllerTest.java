package com.micarch.health;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {
    @Test
    void aggregatesServiceHealth() {
        assertThat(new HealthController().summary()).containsEntry("status", "UP");
    }
}
