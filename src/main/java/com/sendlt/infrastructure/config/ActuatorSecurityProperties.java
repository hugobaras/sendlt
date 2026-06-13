package com.sendlt.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sendlt.actuator")
public record ActuatorSecurityProperties(boolean secured) {}
