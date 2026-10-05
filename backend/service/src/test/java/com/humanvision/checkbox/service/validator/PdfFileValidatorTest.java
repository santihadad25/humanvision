package com.humanvision.checkbox.service.validator;

import static com.humanvision.checkbox.service.PdfFixtures.pdf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.config.ImageLimits;
import com.humanvision.checkbox.service.config.PdfLimits;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;

class PdfFileValidatorTest {
    private final PdfFileValidator validator =
            new PdfFileValidator(new PdfLimits(3, 72), new ImageLimits(2000, 4_000_000));

    @Test
    void acceptsAValidPdf() {
        assertDoesNotThrow(() -> validator.validate(pdf(2, PDRectangle.LETTER)));
    }

    @Test
    void rejectsTooManyPages() {
        assertThrows(InvalidDocumentException.class, () -> validator.validate(pdf(4, PDRectangle.LETTER)));
    }

    @Test
    void rejectsPagesThatWouldRenderTooLarge() {
        assertThrows(InvalidDocumentException.class,
                () -> validator.validate(pdf(1, new PDRectangle(5000, 5000))));
    }

    @Test
    void rejectsCorruptPdf() {
        assertThrows(InvalidDocumentException.class, () -> validator.validate("%PDF-1.4 garbage".getBytes()));
    }
}
