package com.humanvision.checkbox.service.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.pdf")
public record PdfLimits(@Positive int maxPages, @Positive int dpi) {}
