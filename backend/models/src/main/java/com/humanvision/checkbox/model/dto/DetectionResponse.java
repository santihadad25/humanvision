package com.humanvision.checkbox.model.dto;

import com.humanvision.checkbox.model.domain.CheckboxDetectionResult;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import java.util.List;

public record DetectionResponse(List<Box> boxes) {
    public record Box(int[] bbox, boolean isChecked) {}

    public static DetectionResponse from(CheckboxDetectionResult result) {
        return new DetectionResponse(result.pages().stream()
                .flatMap(pageDetection -> pageDetection.boxes().stream())
                .map(DetectionResponse::toBox)
                .toList());
    }

    private static Box toBox(DetectedCheckbox checkbox) {
        return new Box(new int[] {checkbox.x1(), checkbox.y1(), checkbox.x2(), checkbox.y2()}, checkbox.checked());
    }
}
