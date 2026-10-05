package com.humanvision.checkbox.model.contract;

import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import java.util.List;

public interface CheckboxDetector {
    List<DetectedCheckbox> detect(byte[] image);
}
