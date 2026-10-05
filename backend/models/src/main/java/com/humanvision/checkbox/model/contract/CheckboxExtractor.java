package com.humanvision.checkbox.model.contract;

import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.model.domain.PageDetection;
import java.util.List;
import java.util.Set;

public interface CheckboxExtractor {
    Set<FileType> supportedTypes();

    List<PageDetection> extractCheckboxes(byte[] file);
}
