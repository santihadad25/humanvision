package com.humanvision.checkbox.model.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BinaryImageTest {

    private static BinaryImage imageWithInkInTheTopLeftQuarter() {
        boolean[] pixels = new boolean[8 * 8];
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                pixels[row * 8 + column] = true;
            }
        }
        return new BinaryImage(pixels, 8, 8);
    }

    @Test
    void readsInkByColumnAndRow() {
        BinaryImage image = imageWithInkInTheTopLeftQuarter();

        assertTrue(image.isInk(3, 3));
        assertFalse(image.isInk(4, 3));
        assertFalse(image.isInk(3, 4));
        assertEquals(8, image.width());
        assertEquals(8, image.height());
    }

    @Test
    void measuresTheShareOfInkInsideARegion() {
        BinaryImage image = imageWithInkInTheTopLeftQuarter();

        assertEquals(1.0, image.inkShareWithin(new Square(0, 0, 4, 4)));
        assertEquals(0.0, image.inkShareWithin(new Square(4, 4, 8, 8)));
        assertEquals(0.25, image.inkShareWithin(new Square(0, 0, 8, 8)));
    }

    @Test
    void reportsInkInAColumnBetweenRows() {
        BinaryImage image = imageWithInkInTheTopLeftQuarter();

        assertTrue(image.hasInkInColumn(2, 0, 8));
        assertTrue(image.hasInkInColumn(2, 3, 7));
        assertFalse(image.hasInkInColumn(2, 4, 8));
        assertFalse(image.hasInkInColumn(6, 0, 8));
    }

    @Test
    void rejectsAPixelArrayThatDoesNotMatchTheSize() {
        assertThrows(IllegalArgumentException.class, () -> new BinaryImage(new boolean[10], 4, 4));
    }
}
