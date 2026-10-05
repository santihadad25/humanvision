package com.humanvision.checkbox.service.detector.validator;

import com.humanvision.checkbox.model.contract.SquareValidator;
import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SquareSizeValidator implements SquareValidator {
    private static final double NARROWEST_WIDTH_TO_HEIGHT = 0.8;
    private static final double WIDEST_WIDTH_TO_HEIGHT = 1.25;

    private final DetectionProperties properties;

    public SquareSizeValidator(DetectionProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(BinaryImage image, Square square) {
        double widthToHeight = (double) square.width() / square.height();
        return isWithinBoxSizeLimits(square.width())
                && isWithinBoxSizeLimits(square.height())
                && widthToHeight >= NARROWEST_WIDTH_TO_HEIGHT
                && widthToHeight <= WIDEST_WIDTH_TO_HEIGHT;
    }

    private boolean isWithinBoxSizeLimits(int side) {
        return side >= properties.minBoxSize() && side <= properties.maxBoxSize();
    }
}
