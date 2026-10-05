package com.humanvision.checkbox.service.detector.validator;

import com.humanvision.checkbox.model.contract.SquareValidator;
import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class CornersValidator implements SquareValidator {
    private static final double CORNER_AS_SHARE_OF_SIDE = 0.12;
    private static final int MIN_CORNER_SIZE = 2;

    private final DetectionProperties properties;

    public CornersValidator(DetectionProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(BinaryImage image, Square square) {
        int cornerSize = Math.max(MIN_CORNER_SIZE, (int) Math.round(square.averageSide() * CORNER_AS_SHARE_OF_SIDE));
        return square.corners(cornerSize).stream()
                .allMatch(corner -> image.inkShareWithin(corner) >= properties.minCornerInkShare());
    }
}
