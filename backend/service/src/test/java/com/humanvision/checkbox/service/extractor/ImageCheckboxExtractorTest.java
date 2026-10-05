package com.humanvision.checkbox.service.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.model.domain.PageDetection;
import com.humanvision.checkbox.service.ImageFixtures;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImageCheckboxExtractorTest {
    @Test
    void returnsASinglePageWithTheImageSizeAndTheDetectedBoxes() throws Exception {
        var box = new DetectedCheckbox(1, 2, 3, 4, true);
        var extractor = new ImageCheckboxExtractor(image -> List.of(box));

        List<PageDetection> pages = extractor.extractCheckboxes(ImageFixtures.encode(40, 20, "png"));

        assertEquals(List.of(new PageDetection(1, 40, 20, List.of(box))), pages);
    }
}
