package com.humanvision.checkbox.model.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class FileTypeTest {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    private static final byte[] PDF = "%PDF-1.7\n".getBytes();

    @Test
    void detectsEachTypeFromItsSignature() {
        assertEquals(Optional.of(FileType.PNG), FileType.detect(PNG));
        assertEquals(Optional.of(FileType.JPEG), FileType.detect(JPEG));
        assertEquals(Optional.of(FileType.PDF), FileType.detect(PDF));
    }

    @Test
    void unknownOrTooShortContentHasNoType() {
        assertEquals(Optional.empty(), FileType.detect("GIF89a".getBytes()));
        assertEquals(Optional.empty(), FileType.detect("hello".getBytes()));
        assertEquals(Optional.empty(), FileType.detect(new byte[0]));
        assertEquals(Optional.empty(), FileType.detect(null));
        assertEquals(Optional.empty(), FileType.detect(new byte[] {(byte) 0x89, 'P'}));
    }

    @Test
    void matchesOnlyItsOwnSignature() {
        assertTrue(FileType.PNG.matches(PNG));
        assertFalse(FileType.PNG.matches(JPEG));
        assertFalse(FileType.PDF.matches(PNG));
    }

    @Test
    void labelIsLowerCase() {
        assertEquals("jpeg", FileType.JPEG.label());
    }
}
