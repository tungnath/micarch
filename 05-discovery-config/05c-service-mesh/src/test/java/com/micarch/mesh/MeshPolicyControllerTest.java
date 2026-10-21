package com.micarch.mesh;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeshPolicyControllerTest {
    @Test
    void exposesMeshOwnedPolicy() {
        assertThat(new MeshPolicyController().policy()).containsEntry("mtls", "STRICT");
    }
}
