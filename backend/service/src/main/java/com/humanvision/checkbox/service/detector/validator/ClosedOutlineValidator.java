package com.humanvision.checkbox.service.detector.validator;

import com.humanvision.checkbox.model.contract.SquareValidator;
import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class ClosedOutlineValidator implements SquareValidator {
    private static final int PIXELS_TOLERATED_AROUND_A_SIDE = 2;

    private final DetectionProperties properties;

    public ClosedOutlineValidator(DetectionProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(BinaryImage image, Square square) {
        int lastColumn = square.right() - 1;
        int lastRow = square.bottom() - 1;
        return isSideDrawn(square.left(), square.right(), column -> hasInkNearRow(image, column, square.top()))
                && isSideDrawn(square.left(), square.right(), column -> hasInkNearRow(image, column, lastRow))
                && isSideDrawn(square.top(), square.bottom(), row -> hasInkNearColumn(image, row, square.left()))
                && isSideDrawn(square.top(), square.bottom(), row -> hasInkNearColumn(image, row, lastColumn));
    }

    private boolean isSideDrawn(int firstPosition, int endPosition, IntPredicate hasInkAt) {
        int firstChecked = firstPosition + PIXELS_TOLERATED_AROUND_A_SIDE;
        int endChecked = endPosition - PIXELS_TOLERATED_AROUND_A_SIDE;
        long drawnPositions = IntStream.range(firstChecked, endChecked).filter(hasInkAt).count();
        return (double) drawnPositions / (endChecked - firstChecked) >= properties.minOutlineCoverage();
    }

    private static boolean hasInkNearRow(BinaryImage image, int column, int row) {
        int firstRow = Math.max(0, row - PIXELS_TOLERATED_AROUND_A_SIDE);
        int lastRow = Math.min(image.height() - 1, row + PIXELS_TOLERATED_AROUND_A_SIDE);
        return image.hasInkInColumn(column, firstRow, lastRow + 1);
    }

    private static boolean hasInkNearColumn(BinaryImage image, int row, int column) {
        int firstColumn = Math.max(0, column - PIXELS_TOLERATED_AROUND_A_SIDE);
        int lastColumn = Math.min(image.width() - 1, column + PIXELS_TOLERATED_AROUND_A_SIDE);
        return image.hasInkInRow(row, firstColumn, lastColumn + 1);
    }
}
