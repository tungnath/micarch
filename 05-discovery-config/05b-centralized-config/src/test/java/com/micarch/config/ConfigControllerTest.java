package com.micarch.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigControllerTest {
    @Test
    void describesGitBackedConfiguration() {
        assertThat(new ConfigController().info()).containsEntry("backend", "git");
    }
}
