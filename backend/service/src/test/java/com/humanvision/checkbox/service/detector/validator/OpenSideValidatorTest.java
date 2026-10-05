package com.humanvision.checkbox.service.detector.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import com.humanvision.checkbox.service.detector.PageCanvasFactory;
import org.junit.jupiter.api.Test;

class OpenSideValidatorTest {
    private static final Square BOX = new Square(20, 20, 60, 60);

    private final OpenSideValidator validator = new OpenSideValidator(new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 3, 0.02));

    @Test
    void acceptsABoxThatStandsAlone() throws Exception {
        assertTrue(validator.isValid(PageCanvasFactory.pageWithBox(100, 100, 20, 20, 40), BOX));
    }

    @Test
    void rejectsASquareSqueezedBetweenStrokesOnBothSides() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBoxBetweenNeighbourStrokes(100, 100, 20, 20, 40, 2);

        assertFalse(validator.isValid(page, BOX));
    }

    @Test
    void acceptsABoxWithALabelRightNextToOneSideOnly() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBoxAndNeighbourStroke(100, 100, 20, 20, 40, 2);

        assertTrue(validator.isValid(page, BOX));
    }

    @Test
    void acceptsABoxWhoseTopAndBottomAreTableLinesRunningOnToBothSides() throws Exception {
        assertTrue(validator.isValid(PageCanvasFactory.pageWithTableRowAround(200, 100, 20, 40), BOX));
    }

    @Test
    void acceptsABoxStackedOnAnotherOne() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithStackOfBoxes(100, 150, 20, 20, 40, 3);

        assertTrue(validator.isValid(page, BOX));
        assertTrue(validator.isValid(page, new Square(20, 57, 60, 97)));
        assertTrue(validator.isValid(page, new Square(20, 94, 60, 134)));
    }

    @Test
    void acceptsABoxAtTheEdgeOfThePage() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithBox(100, 100, 0, 20, 40);

        assertTrue(validator.isValid(page, new Square(0, 20, 40, 60)));
    }

    @Test
    void isTurnedOffWithAClearanceOfZero() throws Exception {
        var off = new OpenSideValidator(new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 0, 0.02));

        assertTrue(off.isValid(PageCanvasFactory.pageWithBoxBetweenNeighbourStrokes(100, 100, 20, 20, 40, 2), BOX));
    }
}
