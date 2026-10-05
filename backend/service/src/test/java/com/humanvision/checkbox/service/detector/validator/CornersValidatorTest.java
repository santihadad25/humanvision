package com.humanvision.checkbox.service.detector.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import com.humanvision.checkbox.service.detector.PageCanvasFactory;
import org.junit.jupiter.api.Test;

class CornersValidatorTest {
    private static final Square BOX = new Square(20, 20, 60, 60);

    private final CornersValidator validator = new CornersValidator(new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 3, 0.02));

    @Test
    void acceptsABoxDrawnOnAllFourSides() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBox(100, 100, 20, 20, 40);

        assertTrue(validator.isValid(page, BOX));
    }

    @Test
    void rejectsABoldRoundLetterBecauseItsCornersAreEmpty() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBoldLowercaseO(100, 100, 30, 30);

        assertFalse(validator.isValid(page, new Square(33, 34, 53, 56)));
    }

    @Test
    void rejectsAnEmptyPage() {
        assertFalse(validator.isValid(new BinaryImage(new boolean[100 * 100], 100, 100), BOX));
    }
}
