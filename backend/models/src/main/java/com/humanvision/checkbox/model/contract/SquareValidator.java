package com.humanvision.checkbox.model.contract;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;

public interface SquareValidator {
    boolean isValid(BinaryImage image, Square square);
}
