package com.sendlt.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sendlt.jwt")
public record JwtProperties(String secret, long expirationMs) {}
