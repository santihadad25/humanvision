package com.humanvision.checkbox.service.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.ImageFixtures;
import com.humanvision.checkbox.service.config.ImageLimits;
import org.junit.jupiter.api.Test;

class ImageFileValidatorTest {
    private final ImageFileValidator validator = new ImageFileValidator(new ImageLimits(500, 10_000));

    @Test
    void acceptsValidPngAndJpeg() {
        assertDoesNotThrow(() -> validator.validate(ImageFixtures.encode(40, 20, "png")));
        assertDoesNotThrow(() -> validator.validate(ImageFixtures.encode(40, 20, "jpeg")));
    }

    @Test
    void rejectsEmpty() {
        assertThrows(InvalidDocumentException.class, () -> validator.validate(new byte[0]));
    }

    @Test
    void rejectsContentThatIsNotAnImage() {
        assertThrows(InvalidDocumentException.class, () -> validator.validate("not an image".getBytes()));
    }

    @Test
    void rejectsTruncatedFileThatKeepsTheSignature() throws Exception {
        byte[] png = ImageFixtures.encode(40, 20, "png");
        byte[] truncated = java.util.Arrays.copyOf(png, 12);
        assertThrows(InvalidDocumentException.class, () -> validator.validate(truncated));
    }

    @Test
    void rejectsTooManyPixels() throws Exception {
        assertThrows(InvalidDocumentException.class, () -> validator.validate(ImageFixtures.encode(200, 200, "png")));
    }

    @Test
    void rejectsTooLongSide() throws Exception {
        assertThrows(InvalidDocumentException.class, () -> validator.validate(ImageFixtures.encode(501, 1, "png")));
    }
}
