package com.micarch.ambassador;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmbassadorControllerTest {
    @Test
    void adaptsTheExternalProtocol() {
        assertThat(new AmbassadorController().legacyOrder()).containsEntry("protocol", "REST-to-SOAP adapter");
    }
}
