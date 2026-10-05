package com.humanvision.checkbox.service.config;

import com.humanvision.checkbox.model.domain.FileType;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.files")
public record FileTypeProperties(@NotEmpty Set<FileType> allowedTypes) {}
