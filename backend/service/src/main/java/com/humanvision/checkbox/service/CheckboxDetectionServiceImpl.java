package com.humanvision.checkbox.service;

import com.humanvision.checkbox.model.contract.CheckboxDetectionService;
import com.humanvision.checkbox.model.contract.CheckboxExtractor;
import com.humanvision.checkbox.model.contract.FileValidator;
import com.humanvision.checkbox.model.domain.CheckboxDetectionResult;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.model.domain.PageDetection;
import com.humanvision.checkbox.service.config.FileTypeProperties;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckboxDetectionServiceImpl implements CheckboxDetectionService {
    private static final Logger log = LoggerFactory.getLogger(CheckboxDetectionServiceImpl.class);

    private final Set<FileType> enabledTypes;
    private final Map<FileType, FileValidator> validatorByType;
    private final Map<FileType, CheckboxExtractor> extractorByType;

    public CheckboxDetectionServiceImpl(FileTypeProperties fileTypeProperties, List<FileValidator> validators,
                                        List<CheckboxExtractor> extractors) {
        this.enabledTypes = fileTypeProperties.allowedTypes();
        this.validatorByType = indexByType(validators, FileValidator::supportedTypes);
        this.extractorByType = indexByType(extractors, CheckboxExtractor::supportedTypes);
        requireHandlersForEnabledTypes();
    }

    @Override
    public CheckboxDetectionResult detectCheckboxes(byte[] file) {
        FileType fileType = resolveEnabledType(file);

        validatorByType.get(fileType).validate(file);

        long startNanos = System.nanoTime();
        List<PageDetection> pageDetections = extractorByType.get(fileType).extractCheckboxes(file);

        logDetection(fileType, pageDetections, startNanos);
        return new CheckboxDetectionResult(pageDetections);
    }

    private FileType resolveEnabledType(byte[] file) {
        return FileType.detect(file)
                .filter(enabledTypes::contains)
                .orElseThrow(() -> new InvalidDocumentException("Unsupported file type. Allowed: " + enabledTypeLabels()));
    }

    private String enabledTypeLabels() {
        return enabledTypes.stream()
                .sorted(Comparator.naturalOrder())
                .map(FileType::label)
                .collect(Collectors.joining(", "));
    }

    private void requireHandlersForEnabledTypes() {
        List<FileType> unhandledTypes = enabledTypes.stream()
                .filter(fileType -> !validatorByType.containsKey(fileType) || !extractorByType.containsKey(fileType))
                .sorted()
                .toList();
        if (!unhandledTypes.isEmpty()) {
            throw new IllegalStateException("File types " + unhandledTypes + " are enabled but have no validator or extractor.");
        }
    }

    private static void logDetection(FileType fileType, List<PageDetection> pageDetections, long startNanos) {
        long boxCount = pageDetections.stream().mapToLong(page -> page.boxes().size()).sum();
        long checkedCount = pageDetections.stream()
                .flatMap(page -> page.boxes().stream())
                .filter(DetectedCheckbox::checked)
                .count();
        log.atInfo()
                .addKeyValue("file_type", fileType.label())
                .addKeyValue("pages", pageDetections.size())
                .addKeyValue("boxes", boxCount)
                .addKeyValue("checked", checkedCount)
                .addKeyValue("duration_ms", (System.nanoTime() - startNanos) / 1_000_000)
                .log("checkboxes detected");
    }

    private static <T> Map<FileType, T> indexByType(List<T> handlers, Function<T, Set<FileType>> supportedTypesOf) {
        return handlers.stream()
                .flatMap(handler -> supportedTypesOf.apply(handler).stream().map(fileType -> Map.entry(fileType, handler)))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> {
                            throw new IllegalStateException("Two handlers claim the same file type: " + first.getClass().getSimpleName()
                                    + " and " + second.getClass().getSimpleName());
                        }),
                        Map::copyOf));
    }
}
