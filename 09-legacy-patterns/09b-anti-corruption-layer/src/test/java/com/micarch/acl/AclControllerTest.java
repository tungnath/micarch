package com.micarch.acl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AclControllerTest {
    @Test
    void translatesLegacyLanguageToCleanLanguage() {
        var result = new AclController().translate(new AclController.LegacyOrder("L-1", "C-1", "SKU-1", 3));
        assertThat(result.orderId()).isEqualTo("L-1");
        assertThat(result.productId()).isEqualTo("SKU-1");
        assertThat(result.quantity()).isEqualTo(3);
    }
}
