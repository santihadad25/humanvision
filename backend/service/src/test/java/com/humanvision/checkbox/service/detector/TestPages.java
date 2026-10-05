package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.service.config.Binarization;
import com.humanvision.checkbox.service.config.PreprocessingProperties;
import java.util.List;

final class TestPages {
    private static final PageDecoder DECODER = new PageDecoder();
    private static final PageBinarizer GLOBAL_BINARIZER =
            new PageBinarizer(new PreprocessingProperties(Binarization.GLOBAL, 0, 0, List.of(1), 1, Long.MAX_VALUE));

    private TestPages() {}

    static BinaryImage binarize(byte[] encodedImage) {
        return GLOBAL_BINARIZER.binarize(DECODER.decode(encodedImage));
    }
}
