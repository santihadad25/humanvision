package com.humanvision.checkbox.service.extractor;

import com.humanvision.checkbox.model.contract.CheckboxDetector;
import com.humanvision.checkbox.model.contract.CheckboxExtractor;
import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.ImageInfo;
import com.humanvision.checkbox.model.domain.PageDetection;
import com.humanvision.checkbox.service.image.ImageHeaderReader;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ImageCheckboxExtractor implements CheckboxExtractor {
    private static final int SINGLE_PAGE_NUMBER = 1;

    private final CheckboxDetector checkboxDetector;

    public ImageCheckboxExtractor(CheckboxDetector checkboxDetector) {
        this.checkboxDetector = checkboxDetector;
    }

    @Override
    public Set<FileType> supportedTypes() {
        return Set.of(FileType.PNG, FileType.JPEG);
    }

    @Override
    public List<PageDetection> extractCheckboxes(byte[] file) {
        ImageInfo imageInfo = ImageHeaderReader.read(file);
        return List.of(new PageDetection(
                SINGLE_PAGE_NUMBER, imageInfo.width(), imageInfo.height(), checkboxDetector.detect(file)));
    }
}
