package com.humanvision.checkbox.model.dto;

import com.humanvision.checkbox.model.domain.CheckboxDetectionResult;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.model.domain.PageDetection;
import java.util.List;

public record PagedDetectionResponse(List<Box> boxes, List<Page> pages) {
    public record Box(int page, int[] bbox, boolean isChecked) {}

    public record Page(int page, int width, int height) {}

    public static PagedDetectionResponse from(CheckboxDetectionResult result) {
        return new PagedDetectionResponse(toBoxes(result), toPages(result));
    }

    private static List<Box> toBoxes(CheckboxDetectionResult result) {
        return result.pages().stream()
                .flatMap(pageDetection -> pageDetection.boxes().stream()
                        .map(checkbox -> toBox(pageDetection, checkbox)))
                .toList();
    }

    private static Box toBox(PageDetection pageDetection, DetectedCheckbox checkbox) {
        int[] corners = {checkbox.x1(), checkbox.y1(), checkbox.x2(), checkbox.y2()};
        return new Box(pageDetection.pageNumber(), corners, checkbox.checked());
    }

    private static List<Page> toPages(CheckboxDetectionResult result) {
        return result.pages().stream()
                .map(pageDetection -> new Page(pageDetection.pageNumber(), pageDetection.width(), pageDetection.height()))
                .toList();
    }
}
