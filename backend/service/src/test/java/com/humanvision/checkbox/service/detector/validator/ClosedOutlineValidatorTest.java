package com.humanvision.checkbox.service.detector.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import com.humanvision.checkbox.service.detector.PageCanvasFactory;
import org.junit.jupiter.api.Test;

class ClosedOutlineValidatorTest {
    private static final Square BOX = new Square(20, 20, 60, 60);

    private final ClosedOutlineValidator validator =
            new ClosedOutlineValidator(new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 3, 0.02));

    @Test
    void acceptsABoxDrawnOnAllFourSides() throws Exception {
        assertTrue(validator.isValid(PageCanvasFactory.pageWithBox(100, 100, 20, 20, 40), BOX));
    }

    @Test
    void acceptsABoxWhoseTopAndBottomAreLongerTableLines() throws Exception {
        assertTrue(validator.isValid(PageCanvasFactory.pageWithTableRowAround(200, 100, 20, 40), BOX));
    }

    @Test
    void rejectsASquareWithoutABottom() throws Exception {
        assertFalse(validator.isValid(PageCanvasFactory.pageWithoutBottom(100, 100, 20, 20, 40), BOX));
    }

    @Test
    void rejectsAnOutlineWithASinglePixelGapInTheTop() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBoxHavingAGap(100, 100, 20, 20, 40, 38, 20, 1, 3);

        assertFalse(validator.isValid(page, BOX));
    }

    @Test
    void rejectsAnOutlineWithAGapInTheLeftSide() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBoxHavingAGap(100, 100, 20, 20, 40, 20, 38, 3, 1);

        assertFalse(validator.isValid(page, BOX));
    }

    @Test
    void rejectsAnOutlineWithAGapInTheRightSideAndInTheBottom() throws Exception {
        assertFalse(validator.isValid(PageCanvasFactory.pageWithBoxHavingAGap(100, 100, 20, 20, 40, 57, 38, 3, 1), BOX));
        assertFalse(validator.isValid(PageCanvasFactory.pageWithBoxHavingAGap(100, 100, 20, 20, 40, 38, 57, 1, 3), BOX));
    }

    @Test
    void ignoresAGapInTheLastPixelsOfASideBecauseThoseAreTheCorners() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBoxHavingAGap(100, 100, 20, 20, 40, 20, 20, 2, 1);

        assertTrue(validator.isValid(page, BOX));
    }

    @Test
    void aSideIsToleratedAsDrawnWhenTheGapIsOnlyAsWideAsTheToleratedOffset() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBoxHavingAGap(100, 100, 20, 20, 40, 38, 20, 1, 1);

        assertTrue(validator.isValid(page, BOX));
    }

    @Test
    void acceptsAnOutlineWithGapsWhenTheRequiredCoverageIsRelaxed() throws Exception {
        var relaxed = new ClosedOutlineValidator(new DetectionProperties(20, 100, 0.9, 0.07, 0.4, 3, 0.02));
        BinaryImage page = PageCanvasFactory.pageWithBoxHavingAGap(100, 100, 20, 20, 40, 38, 20, 1, 3);

        assertTrue(relaxed.isValid(page, BOX));
    }
}
