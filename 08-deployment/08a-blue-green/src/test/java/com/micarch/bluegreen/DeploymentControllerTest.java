package com.micarch.bluegreen;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentControllerTest {
    @Test
    void greenIsActiveInitially() {
        assertThat(new DeploymentController().state()).containsEntry("active", "green");
    }
}
