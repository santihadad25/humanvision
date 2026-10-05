package com.humanvision.checkbox.service.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.detection")
public record DetectionProperties(
        @Positive int minBoxSize,
        @Positive int maxBoxSize,
        @DecimalMin("0.0") @DecimalMax("1.0") double minOutlineCoverage,
        @DecimalMin("0.0") @DecimalMax("1.0") double checkedInkShare,
        @DecimalMin("0.0") @DecimalMax("1.0") double minCornerInkShare,
        @PositiveOrZero int sideClearance,
        @DecimalMin("0.0") @DecimalMax("1.0") double maxSideInkShare) {

    public DetectionProperties {
        if (maxBoxSize < minBoxSize) {
            throw new IllegalArgumentException("max-box-size must not be smaller than min-box-size");
        }
    }
}
