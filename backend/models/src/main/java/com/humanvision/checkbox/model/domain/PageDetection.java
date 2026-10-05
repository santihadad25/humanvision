package com.humanvision.checkbox.model.domain;

import java.util.List;

public record PageDetection(int pageNumber, int width, int height, List<DetectedCheckbox> boxes) {}
