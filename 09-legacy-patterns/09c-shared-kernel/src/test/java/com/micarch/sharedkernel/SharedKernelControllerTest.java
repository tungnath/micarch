package com.micarch.sharedkernel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharedKernelControllerTest {
    @Test
    void exposesASharedValueObjectInvariant() {
        assertThat(new SharedKernelController().money()).containsEntry("currency", "USD");
    }
}
