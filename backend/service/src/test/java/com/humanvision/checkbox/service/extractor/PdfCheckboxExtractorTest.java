package com.humanvision.checkbox.service.extractor;

import static com.humanvision.checkbox.service.PdfFixtures.pdf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.model.domain.PageDetection;
import com.humanvision.checkbox.service.config.PdfLimits;
import java.util.List;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;

class PdfCheckboxExtractorTest {
    @Test
    void rasterizesEveryPageAndRunsTheDetectorOnEach() throws Exception {
        var box = new DetectedCheckbox(1, 2, 3, 4, false);
        var extractor = new PdfCheckboxExtractor(new PdfPageRasterizer(new PdfLimits(3, 72)), image -> List.of(box));

        List<PageDetection> pages = extractor.extractCheckboxes(pdf(2, PDRectangle.LETTER));

        assertEquals(2, pages.size());
        assertEquals(1, pages.get(0).pageNumber());
        assertEquals(2, pages.get(1).pageNumber());
        assertEquals(612, pages.get(0).width());
        assertEquals(792, pages.get(0).height());
        assertEquals(List.of(box), pages.get(1).boxes());
    }
}
