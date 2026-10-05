package com.humanvision.checkbox.service.extractor;

import com.humanvision.checkbox.model.contract.CheckboxDetector;
import com.humanvision.checkbox.model.contract.CheckboxExtractor;
import com.humanvision.checkbox.model.domain.DocumentPage;
import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.PageDetection;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PdfCheckboxExtractor implements CheckboxExtractor {
    private final PdfPageRasterizer pageRasterizer;
    private final CheckboxDetector checkboxDetector;

    public PdfCheckboxExtractor(PdfPageRasterizer pageRasterizer, CheckboxDetector checkboxDetector) {
        this.pageRasterizer = pageRasterizer;
        this.checkboxDetector = checkboxDetector;
    }

    @Override
    public Set<FileType> supportedTypes() {
        return Set.of(FileType.PDF);
    }

    @Override
    public List<PageDetection> extractCheckboxes(byte[] file) {
        return pageRasterizer.rasterize(file).stream().map(this::detectCheckboxesOnPage).toList();
    }

    private PageDetection detectCheckboxesOnPage(DocumentPage page) {
        return new PageDetection(
                page.pageNumber(), page.width(), page.height(), checkboxDetector.detect(page.image()));
    }
}
