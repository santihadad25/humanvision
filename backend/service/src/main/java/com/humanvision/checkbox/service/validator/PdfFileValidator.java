package com.humanvision.checkbox.service.validator;

import com.humanvision.checkbox.model.contract.FileValidator;
import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.config.ImageLimits;
import com.humanvision.checkbox.service.config.PdfLimits;
import java.io.IOException;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PdfFileValidator implements FileValidator {
    private static final Logger log = LoggerFactory.getLogger(PdfFileValidator.class);
    private static final double POINTS_PER_INCH = 72.0;

    private final PdfLimits pdfLimits;
    private final ImageLimits imageLimits;

    public PdfFileValidator(PdfLimits pdfLimits, ImageLimits imageLimits) {
        this.pdfLimits = pdfLimits;
        this.imageLimits = imageLimits;
    }

    @Override
    public Set<FileType> supportedTypes() {
        return Set.of(FileType.PDF);
    }

    @Override
    public void validate(byte[] file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            validatePageCount(document.getNumberOfPages());
            validateRenderedPageSizes(document);
            log.atInfo().addKeyValue("pages", document.getNumberOfPages()).log("pdf validated");
        } catch (InvalidPasswordException exception) {
            throw new InvalidDocumentException("Password-protected PDFs are not supported.");
        } catch (InvalidDocumentException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new InvalidDocumentException("PDF is corrupt or could not be read.");
        }
    }

    private void validatePageCount(int pageCount) {
        if (pageCount == 0) {
            throw new InvalidDocumentException("PDF has no pages.");
        }
        if (pageCount > pdfLimits.maxPages()) {
            throw new InvalidDocumentException(
                    "PDF has " + pageCount + " pages; the maximum is " + pdfLimits.maxPages() + ".");
        }
    }

    private void validateRenderedPageSizes(PDDocument document) {
        IntStream.range(0, document.getNumberOfPages())
                .forEach(pageIndex -> validateRenderedSize(pageIndex + 1, document.getPage(pageIndex).getMediaBox()));
    }

    private void validateRenderedSize(int pageNumber, PDRectangle mediaBox) {
        double pixelsPerPoint = pdfLimits.dpi() / POINTS_PER_INCH;
        long renderedWidth = (long) Math.ceil(mediaBox.getWidth() * pixelsPerPoint);
        long renderedHeight = (long) Math.ceil(mediaBox.getHeight() * pixelsPerPoint);
        if (exceedsImageLimits(renderedWidth, renderedHeight)) {
            throw new InvalidDocumentException("PDF page " + pageNumber + " would render at " + renderedWidth + "x"
                    + renderedHeight + ", exceeding the allowed limits.");
        }
    }

    private boolean exceedsImageLimits(long width, long height) {
        return width <= 0 || height <= 0
                || width > imageLimits.maxDimension()
                || height > imageLimits.maxDimension()
                || width * height > imageLimits.maxPixels();
    }
}
