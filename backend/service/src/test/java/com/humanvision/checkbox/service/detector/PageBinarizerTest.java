package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import org.junit.jupiter.api.Test;

class PageBinarizerTest {
    @Test
    void separatesDarkInkFromLightPaper() throws Exception {
        BinaryImage image = TestPages.binarize(new PageCanvas(50, 50).box(10, 10, 30).toPng());

        assertTrue(image.isInk(10, 20));
        assertFalse(image.isInk(25, 25));
        assertEquals(50, image.width());
        assertEquals(50, image.height());
    }

    @Test
    void rejectsBytesThatAreNotAnImage() {
        assertThrows(InvalidDocumentException.class, () -> TestPages.binarize("not an image".getBytes()));
    }
}
