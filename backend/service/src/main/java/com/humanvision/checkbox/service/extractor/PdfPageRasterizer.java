package com.humanvision.checkbox.service.extractor;

import com.humanvision.checkbox.model.domain.DocumentPage;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.logging.RejectedInputLog;
import com.humanvision.checkbox.service.config.PdfLimits;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PdfPageRasterizer {
    private static final Logger log = LoggerFactory.getLogger(PdfPageRasterizer.class);

    private final PdfLimits pdfLimits;

    public PdfPageRasterizer(PdfLimits pdfLimits) {
        this.pdfLimits = pdfLimits;
    }

    public List<DocumentPage> rasterize(byte[] pdfBytes) {
        long startNanos = System.nanoTime();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            List<DocumentPage> pages = new ArrayList<>(pageCount);
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                pages.add(renderPage(renderer, pageIndex));
            }
            logRasterized(pageCount, startNanos);
            return pages;
        } catch (IOException | RuntimeException exception) {
            RejectedInputLog.warn(log, "pdf could not be rasterized", exception);
            throw new InvalidDocumentException("PDF is corrupt or could not be read.");
        }
    }

    private DocumentPage renderPage(PDFRenderer renderer, int pageIndex) throws IOException {
        BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, pdfLimits.dpi(), ImageType.RGB);
        return new DocumentPage(pageIndex + 1, encodeAsPng(pageImage), pageImage.getWidth(), pageImage.getHeight());
    }

    private void logRasterized(int pageCount, long startNanos) {
        log.atInfo()
                .addKeyValue("pages", pageCount)
                .addKeyValue("dpi", pdfLimits.dpi())
                .addKeyValue("duration_ms", (System.nanoTime() - startNanos) / 1_000_000)
                .log("pdf rasterized");
    }

    private static byte[] encodeAsPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream pngBytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", pngBytes);
        return pngBytes.toByteArray();
    }
}
