package com.micarch.sidecar;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SidecarControllerTest {
    @Test
    void policyIsOffloadedFromApplicationCode() {
        assertThat(new SidecarController().policy()).containsEntry("proxy", "envoy").containsEntry("mTLS", true);
    }
}
