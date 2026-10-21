package com.micarch.security.gateway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayPolicyControllerTest {
    @Test
    void defaultsToProtectedRoutes() {
        assertThat(new GatewayPolicyController().policy()).containsEntry("protectedDefault", true);
    }
}
