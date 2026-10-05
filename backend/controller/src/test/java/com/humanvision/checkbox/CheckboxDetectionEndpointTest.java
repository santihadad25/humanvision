package com.humanvision.checkbox;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CheckboxDetectionEndpointTest {
    @Autowired MockMvc mvc;

    private static byte[] png(int w, int h) throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB), "png", out);
        return out.toByteArray();
    }

    @Test
    void validPngReturnsBoxesAndSize() throws Exception {
        mvc.perform(multipart("/v2/detect").file(new MockMultipartFile("file", "a.png", "image/png", png(40, 20))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxes").isArray())
                .andExpect(jsonPath("$.pages.length()").value(1))
                .andExpect(jsonPath("$.pages[0].page").value(1))
                .andExpect(jsonPath("$.pages[0].width").value(40))
                .andExpect(jsonPath("$.pages[0].height").value(20));
    }

    @Test
    void pdfReturnsOnePageEntryPerPage() throws Exception {
        byte[] pdf = ("%PDF-1.4\n"
                + "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                + "2 0 obj<</Type/Pages/Kids[3 0 R 4 0 R]/Count 2>>endobj\n"
                + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
                + "4 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
                + "trailer<</Root 1 0 R/Size 5>>\n%%EOF\n").getBytes();
        mvc.perform(multipart("/v2/detect").file(new MockMultipartFile("file", "a.pdf", "application/pdf", pdf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pages.length()").value(2))
                .andExpect(jsonPath("$.pages[1].page").value(2))
                .andExpect(jsonPath("$.pages[0].width").value(2550))
                .andExpect(jsonPath("$.pages[0].height").value(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(3299), org.hamcrest.Matchers.is(3300))));
    }

    @Test
    void challengeEndpointAnswersOnlyWithTheBoxesList() throws Exception {
        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.png", "image/png", png(40, 20))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxes").isArray())
                .andExpect(jsonPath("$.pages").doesNotExist());
    }

    @Test
    void bothEndpointsRejectAnInvalidFile() throws Exception {
        for (String path : new String[] {"/detect", "/v2/detect"}) {
            mvc.perform(multipart(path).file(new MockMultipartFile("file", "a.txt", "text/plain", "hi".getBytes())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void corruptPdfIs400() throws Exception {
        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.pdf", "application/pdf", "%PDF-1.4 junk".getBytes())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonImageContentIs400() throws Exception {
        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.txt", "text/plain", "hi".getBytes())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void spoofedContentTypeIs400() throws Exception {
        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.png", "image/png", "not an image".getBytes())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyFileIs400() throws Exception {
        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.png", "image/png", new byte[0])))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptHeaderExcludingJsonIs406NotA500() throws Exception {
        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.png", "image/png", png(4, 4)))
                        .header("Accept", "text/html"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void responsesCarrySecurityHeaders() throws Exception {
        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.png", "image/png", png(4, 4))))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void safeRequestIdIsEchoedAndUnsafeOneIsReplaced() throws Exception {
        var file = new MockMultipartFile("file", "a.png", "image/png", png(4, 4));
        mvc.perform(multipart("/detect").file(file).header("X-Request-Id", "client-id-1"))
                .andExpect(header().string("X-Request-Id", "client-id-1"));
        mvc.perform(multipart("/detect").file(file).header("X-Request-Id", "bad id with spaces\t{}"))
                .andExpect(header().string("X-Request-Id", org.hamcrest.Matchers.matchesPattern("[0-9a-f-]{36}")));
    }

    @Test
    void missingFileIs400() throws Exception {
        mvc.perform(multipart("/detect")).andExpect(status().isBadRequest());
    }
}
