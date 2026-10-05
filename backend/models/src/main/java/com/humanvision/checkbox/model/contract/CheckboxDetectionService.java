package com.humanvision.checkbox.model.contract;

import com.humanvision.checkbox.model.domain.CheckboxDetectionResult;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;

public interface CheckboxDetectionService {
    CheckboxDetectionResult detectCheckboxes(byte[] document);
}
