package com.humanvision.checkbox.service.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.image")
public record ImageLimits(@Positive int maxDimension, @Positive long maxPixels) {}
