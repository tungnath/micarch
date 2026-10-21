package com.micarch.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Small demonstration authentication filter. A production gateway would
 * validate a signed JWT or delegate to an OAuth2 resource-server configuration.
 */
@Component
public class AuthenticationFilterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationFilterGatewayFilterFactory.Config> {

    private final String expectedToken;

    public AuthenticationFilterGatewayFilterFactory(
            @Value("${gateway.security.demo-token:demo-token}") String expectedToken) {
        super(Config.class);
        this.expectedToken = expectedToken;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (!("Bearer " + expectedToken).equals(authorization)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
