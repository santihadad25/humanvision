package com.humanvision.checkbox.model.dto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.domain.CheckboxDetectionResult;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.model.domain.PageDetection;
import java.util.List;
import org.junit.jupiter.api.Test;

class DetectionResponseTest {
    private final CheckboxDetectionResult twoPages = new CheckboxDetectionResult(List.of(
            new PageDetection(1, 100, 200, List.of(new DetectedCheckbox(1, 2, 3, 4, true), new DetectedCheckbox(5, 6, 7, 8, false))),
            new PageDetection(2, 100, 200, List.of(new DetectedCheckbox(9, 10, 11, 12, true)))));

    @Test
    void challengeResponseListsTheBoxesOfEveryPageOneAfterAnotherInPageOrder() {
        List<DetectionResponse.Box> boxes = DetectionResponse.from(twoPages).boxes();

        assertEquals(3, boxes.size());
        assertArrayEquals(new int[] {1, 2, 3, 4}, boxes.get(0).bbox());
        assertTrue(boxes.get(0).isChecked());
        assertArrayEquals(new int[] {5, 6, 7, 8}, boxes.get(1).bbox());
        assertFalse(boxes.get(1).isChecked());
        assertArrayEquals(new int[] {9, 10, 11, 12}, boxes.get(2).bbox());
    }

    @Test
    void challengeResponseOfADocumentWithoutCheckboxesHasAnEmptyList() {
        CheckboxDetectionResult empty = new CheckboxDetectionResult(List.of(new PageDetection(1, 10, 10, List.of())));

        assertTrue(DetectionResponse.from(empty).boxes().isEmpty());
    }

    @Test
    void pagedResponseKeepsThePageOfEachBoxAndTheSizeOfEveryPage() {
        PagedDetectionResponse response = PagedDetectionResponse.from(twoPages);

        assertEquals(List.of(1, 1, 2), response.boxes().stream().map(PagedDetectionResponse.Box::page).toList());
        assertEquals(List.of(1, 2), response.pages().stream().map(PagedDetectionResponse.Page::page).toList());
        assertEquals(200, response.pages().get(0).height());
    }
}
