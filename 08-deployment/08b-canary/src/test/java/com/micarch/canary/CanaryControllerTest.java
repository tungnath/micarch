package com.micarch.canary;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CanaryControllerTest {
    @Test
    void canIncreaseCanaryWeight() {
        var controller = new CanaryController();
        assertThat(controller.setWeight(20)).containsEntry("stableWeightPercent", 80);
    }
}
