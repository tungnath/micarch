package com.micarch.registry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryControllerTest {
    @Test
    void describesEurekaServer() {
        assertThat(new RegistryController().info()).containsEntry("registry", "eureka");
    }
}
