package com.humanvision.checkbox.service.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.preprocessing")
public record PreprocessingProperties(
        @NotNull Binarization binarization,
        @PositiveOrZero int localWindow,
        @PositiveOrZero int localDelta,
        @NotEmpty List<@Positive Integer> upscaleFactors,
        @Positive int enoughBoxes,
        @Positive long maxWorkingPixels) {}
