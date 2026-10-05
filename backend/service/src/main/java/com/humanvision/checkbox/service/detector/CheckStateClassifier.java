package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.service.config.DetectionProperties;

final class CheckStateClassifier {
    private static final double BORDER_AS_SHARE_OF_SIDE = 0.24;
    private static final int MIN_BORDER_MARGIN = 4;

    private final double inkShareThatMeansChecked;

    CheckStateClassifier(DetectionProperties properties) {
        this.inkShareThatMeansChecked = properties.checkedInkShare();
    }

    boolean isChecked(BinaryImage image, Square checkbox) {
        return checkbox.shrunkBy(borderMarginOf(checkbox))
                .map(interior -> image.inkShareWithin(interior) > inkShareThatMeansChecked)
                .orElse(false);
    }

    private static int borderMarginOf(Square checkbox) {
        int shortestSide = Math.min(checkbox.width(), checkbox.height());
        return Math.max(MIN_BORDER_MARGIN, (int) (BORDER_AS_SHARE_OF_SIDE * shortestSide));
    }
}
