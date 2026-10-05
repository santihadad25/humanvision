package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class SampleDocumentsTest {
    private final ClassicCheckboxDetector detector =
            StandardDetector.create();

    private List<DetectedCheckbox> detectIn(String sample) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/samples/" + sample)) {
            return detector.detect(stream.readAllBytes());
        }
    }

    private static void assertCount(List<DetectedCheckbox> boxes, int min, int max) {
        assertTrue(boxes.size() >= min && boxes.size() <= max,
                "expected between " + min + " and " + max + " boxes but found " + boxes.size());
    }

    private static void assertCheckedCount(List<DetectedCheckbox> boxes, int min, int max) {
        long checked = boxes.stream().filter(DetectedCheckbox::checked).count();
        assertTrue(checked >= min && checked <= max, "expected " + min + " to " + max + " checked but found " + checked);
    }

    private static void assertAllSidesBetween(List<DetectedCheckbox> boxes, int min, int max) {
        boxes.forEach(box -> {
            int side = ((box.x2() - box.x1()) + (box.y2() - box.y1())) / 2;
            assertTrue(side >= min && side <= max, "unexpected box side " + side);
        });
    }

    @Test
    void cleanUniformResidentialForm() throws IOException {
        List<DetectedCheckbox> boxes = detectIn("uniform-residential-clean.png");

        assertCount(boxes, 100, 112);
        assertCheckedCount(boxes, 32, 38);
        assertAllSidesBetween(boxes, 48, 58);
    }

    @Test
    void degradedCropDoesNotMistakeLettersForCheckboxes() throws IOException {
        List<DetectedCheckbox> boxes = detectIn("degraded-crop.png");

        assertCount(boxes, 36, 46);
        assertAllSidesBetween(boxes, 22, 26);
    }

    @Test
    void tableWithShadedCells() throws IOException {
        List<DetectedCheckbox> boxes = detectIn("market-conditions-blue-cells.png");

        assertCount(boxes, 45, 52);
        assertCheckedCount(boxes, 10, 14);
        assertAllSidesBetween(boxes, 50, 54);
    }

    @Test
    void screenshotOfTheApplicationWithItsOwnFramesAroundTheBoxes() throws IOException {
        List<DetectedCheckbox> boxes = detectIn("ui-overlay-crop.png");

        assertCount(boxes, 4, 4);
        assertCheckedCount(boxes, 1, 1);
    }

    @Test
    void formWithWatermark() throws IOException {
        List<DetectedCheckbox> boxes = detectIn("manufactured-home-watermark.png");

        assertCount(boxes, 74, 84);
        assertCheckedCount(boxes, 25, 29);
        assertAllSidesBetween(boxes, 28, 34);
    }
}
