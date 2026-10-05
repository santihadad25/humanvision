package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.service.config.Binarization;
import com.humanvision.checkbox.service.config.DetectionProperties;
import com.humanvision.checkbox.service.config.PreprocessingProperties;
import com.humanvision.checkbox.service.detector.validator.OpenSideValidator;
import com.humanvision.checkbox.service.detector.validator.CornersValidator;
import com.humanvision.checkbox.service.detector.validator.SquareSizeValidator;
import com.humanvision.checkbox.service.detector.validator.ClosedOutlineValidator;
import java.util.List;

final class StandardDetector {
    static final DetectionProperties PROPERTIES = new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 3, 0.02);

    static final PreprocessingProperties PREPROCESSING =
            new PreprocessingProperties(Binarization.BOTH, 25, 12, List.of(1, 2, 3, 4), 5, 40_000_000L);

    private StandardDetector() {}

    static ClassicCheckboxDetector create() {
        return create(PREPROCESSING);
    }

    static ClassicCheckboxDetector create(PreprocessingProperties preprocessing) {
        return new ClassicCheckboxDetector(PROPERTIES, preprocessing, List.of(
                new SquareSizeValidator(PROPERTIES),
                new CornersValidator(PROPERTIES),
                new OpenSideValidator(PROPERTIES),
                new ClosedOutlineValidator(PROPERTIES)));
    }
}
