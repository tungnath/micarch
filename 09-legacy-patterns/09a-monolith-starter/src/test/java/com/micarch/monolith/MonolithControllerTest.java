package com.micarch.monolith;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonolithControllerTest {
    @Test
    void oneApplicationCanOwnTheWholeFlow() {
        var controller = new MonolithController();
        controller.place(new MonolithController.Order("O-1", "C-1", "P-1", 1, "PENDING"));
        assertThat(controller.find("O-1").customerId()).isEqualTo("C-1");
    }
}
