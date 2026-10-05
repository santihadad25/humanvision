package com.humanvision.checkbox.service.detector.validator;

import com.humanvision.checkbox.model.contract.SquareValidator;
import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class OpenSideValidator implements SquareValidator {
    private static final double TOP_AND_BOTTOM_ROWS_SKIPPED_AS_SHARE_OF_HEIGHT = 0.25;

    private final DetectionProperties properties;

    public OpenSideValidator(DetectionProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(BinaryImage image, Square square) {
        if (properties.sideClearance() == 0) {
            return true;
        }
        int skippedRows = (int) Math.round(square.height() * TOP_AND_BOTTOM_ROWS_SKIPPED_AS_SHARE_OF_HEIGHT);
        int firstRow = square.top() + skippedRows;
        int endRow = square.bottom() - skippedRows;
        Square leftStrip = new Square(square.left() - properties.sideClearance(), firstRow, square.left(), endRow);
        Square rightStrip = new Square(square.right(), firstRow, square.right() + properties.sideClearance(), endRow);
        return isOpen(image, leftStrip) || isOpen(image, rightStrip);
    }

    private boolean isOpen(BinaryImage image, Square strip) {
        Square visible = new Square(
                Math.max(0, strip.left()), strip.top(), Math.min(image.width(), strip.right()), strip.bottom());
        boolean isEmpty = visible.width() <= 0 || visible.height() <= 0;
        return isEmpty || image.inkShareWithin(visible) <= properties.maxSideInkShare();
    }
}
