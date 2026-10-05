package com.humanvision.checkbox.service;

import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public final class PdfFixtures {
    private PdfFixtures() {}

    public static byte[] pdf(int pages, PDRectangle size) throws Exception {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 0; i < pages; i++) doc.addPage(new PDPage(size));
            doc.save(out);
            return out.toByteArray();
        }
    }
}
