package com.humanvision.checkbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.contract.CheckboxExtractor;
import com.humanvision.checkbox.model.contract.FileValidator;
import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.model.domain.PageDetection;
import com.humanvision.checkbox.service.config.FileTypeProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class CheckboxDetectionServiceImplTest {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] PDF = "%PDF-1.7".getBytes();
    private static final byte[] GIF = "GIF89a".getBytes();

    private final List<String> calls = new ArrayList<>();

    private FileValidator validator(String name, Set<FileType> types, Consumer<byte[]> action) {
        return new FileValidator() {
            @Override public Set<FileType> supportedTypes() { return types; }
            @Override public void validate(byte[] file) { calls.add("validate:" + name); action.accept(file); }
        };
    }

    private CheckboxExtractor extractor(String name, Set<FileType> types) {
        return new CheckboxExtractor() {
            @Override public Set<FileType> supportedTypes() { return types; }
            @Override public List<PageDetection> extractCheckboxes(byte[] file) {
                calls.add("extract:" + name);
                return List.of(new PageDetection(1, 10, 10, List.of()));
            }
        };
    }

    private CheckboxDetectionServiceImpl service(Set<FileType> allowed, Consumer<byte[]> pngValidation) {
        return new CheckboxDetectionServiceImpl(
                new FileTypeProperties(allowed),
                List.of(validator("image", Set.of(FileType.PNG, FileType.JPEG), pngValidation),
                        validator("pdf", Set.of(FileType.PDF), file -> {})),
                List.of(extractor("image", Set.of(FileType.PNG, FileType.JPEG)),
                        extractor("pdf", Set.of(FileType.PDF))));
    }

    @Test
    void validatesThenExtractsWithTheHandlersOfTheDetectedType() {
        var result = service(Set.of(FileType.PNG, FileType.PDF), file -> {}).detectCheckboxes(PDF);

        assertEquals(List.of("validate:pdf", "extract:pdf"), calls);
        assertEquals(1, result.pages().size());
    }

    @Test
    void usesTheImageHandlersForPng() {
        service(Set.of(FileType.PNG), file -> {}).detectCheckboxes(PNG);
        assertEquals(List.of("validate:image", "extract:image"), calls);
    }

    @Test
    void doesNotExtractWhenValidationFails() {
        var service = service(Set.of(FileType.PNG), file -> { throw new InvalidDocumentException("bad"); });

        assertThrows(InvalidDocumentException.class, () -> service.detectCheckboxes(PNG));
        assertEquals(List.of("validate:image"), calls);
    }

    @Test
    void rejectsUnknownFileTypes() {
        var service = service(Set.of(FileType.PNG), file -> {});
        var exception = assertThrows(InvalidDocumentException.class, () -> service.detectCheckboxes(GIF));
        assertTrue(exception.getMessage().contains("Unsupported file type"));
        assertTrue(calls.isEmpty());
    }

    @Test
    void rejectsTypesThatAreNotEnabledInTheConfiguration() {
        var service = service(Set.of(FileType.PNG), file -> {});
        assertThrows(InvalidDocumentException.class, () -> service.detectCheckboxes(PDF));
        assertFalse(calls.contains("extract:pdf"));
    }

    @Test
    void failsAtStartupWhenAnEnabledTypeHasNoHandlers() {
        assertThrows(IllegalStateException.class, () -> new CheckboxDetectionServiceImpl(
                new FileTypeProperties(Set.of(FileType.PNG, FileType.PDF)),
                List.of(validator("image", Set.of(FileType.PNG), file -> {})),
                List.of(extractor("image", Set.of(FileType.PNG)))));
    }

    @Test
    void failsAtStartupWhenTwoHandlersClaimTheSameType() {
        assertThrows(IllegalStateException.class, () -> new CheckboxDetectionServiceImpl(
                new FileTypeProperties(Set.of(FileType.PNG)),
                List.of(validator("a", Set.of(FileType.PNG), file -> {}), validator("b", Set.of(FileType.PNG), file -> {})),
                List.of(extractor("a", Set.of(FileType.PNG)))));
    }
}
