package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import org.junit.jupiter.api.Test;

class CheckStateClassifierTest {
    private static final Square BOX = new Square(20, 20, 60, 60);

    private final CheckStateClassifier classifier = new CheckStateClassifier(new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 3, 0.02));

    @Test
    void emptyBoxIsNotChecked() throws Exception {
        BinaryImage page = TestPages.binarize(new PageCanvas(100, 100).box(20, 20, 40).toPng());

        assertFalse(classifier.isChecked(page, BOX));
    }

    @Test
    void boxWithACrossIsChecked() throws Exception {
        BinaryImage page = TestPages.binarize(new PageCanvas(100, 100).checkedBox(20, 20, 40).toPng());

        assertTrue(classifier.isChecked(page, BOX));
    }

    @Test
    void aBoxTooSmallToHaveAnInteriorIsNotChecked() throws Exception {
        BinaryImage page = TestPages.binarize(new PageCanvas(100, 100).filledBlock(20, 20, 6, 6).toPng());

        assertFalse(classifier.isChecked(page, new Square(20, 20, 26, 26)));
    }
}
