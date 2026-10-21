package com.micarch.security.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityControllerTest {
    @Test
    void exposesAuthenticatedPrincipal() {
        var authentication = new UsernamePasswordAuthenticationToken("alice", "n/a");
        assertThat(new SecurityController().profile(authentication)).containsEntry("principal", "alice");
    }
}
