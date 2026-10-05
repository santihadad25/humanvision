package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.contract.CheckboxDetector;
import com.humanvision.checkbox.model.contract.SquareValidator;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.service.config.DetectionProperties;
import com.humanvision.checkbox.service.config.PreprocessingProperties;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClassicCheckboxDetector implements CheckboxDetector {
    private final PreprocessingProperties preprocessing;
    private final PageDecoder decoder = new PageDecoder();
    private final SingleScaleDetector scaleDetector;

    public ClassicCheckboxDetector(DetectionProperties properties, PreprocessingProperties preprocessing,
                                   List<SquareValidator> squareValidators) {
        this.preprocessing = preprocessing;
        this.scaleDetector = new SingleScaleDetector(properties, preprocessing, squareValidators);
    }

    @Override
    public List<DetectedCheckbox> detect(byte[] encodedImage) {
        GrayPage page = decoder.decode(encodedImage);
        List<DetectedCheckbox> best = List.of();
        for (int factor : preprocessing.upscaleFactors()) {
            if (factor > 1 && page.pixelCountWhenScaledBy(factor) > preprocessing.maxWorkingPixels()) {
                continue;
            }
            List<DetectedCheckbox> found = scaleDetector.detect(page.scaledBy(factor), factor);
            if (found.size() > best.size()) {
                best = found;
            }
            if (found.size() >= preprocessing.enoughBoxes()) {
                break;
            }
        }
        return best;
    }
}
