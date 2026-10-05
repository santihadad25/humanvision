package com.humanvision.checkbox.model.contract;

import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import java.util.Set;

public interface FileValidator {
    Set<FileType> supportedTypes();

    void validate(byte[] file);
}
