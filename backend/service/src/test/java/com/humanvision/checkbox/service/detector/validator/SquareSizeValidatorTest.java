package com.humanvision.checkbox.service.detector.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import org.junit.jupiter.api.Test;

class SquareSizeValidatorTest {
    private static final BinaryImage ANY_IMAGE = new BinaryImage(new boolean[1], 1, 1);

    private final SquareSizeValidator validator = new SquareSizeValidator(new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 3, 0.02));

    private boolean isValid(int width, int height) {
        return validator.isValid(ANY_IMAGE, new Square(0, 0, width, height));
    }

    @Test
    void acceptsSquaresWithinTheSizeLimits() {
        assertTrue(isValid(40, 40));
        assertTrue(isValid(20, 20));
        assertTrue(isValid(100, 100));
    }

    @Test
    void rejectsSquaresSmallerOrLargerThanTheLimits() {
        assertFalse(isValid(19, 19));
        assertFalse(isValid(101, 101));
    }

    @Test
    void rejectsRectanglesThatAreNotSquare() {
        assertFalse(isValid(50, 30));
        assertFalse(isValid(30, 50));
        assertTrue(isValid(40, 35));
    }
}
