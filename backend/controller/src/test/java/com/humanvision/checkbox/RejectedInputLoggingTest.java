package com.humanvision.checkbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RejectedInputLoggingTest {
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    @Autowired MockMvc mvc;

    private final Logger serviceLogger = (Logger) LoggerFactory.getLogger("com.humanvision.checkbox.service");
    private final ListAppender<ILoggingEvent> logged = new ListAppender<>();

    @BeforeEach
    void captureServiceLogs() {
        logged.start();
        serviceLogger.addAppender(logged);
    }

    @AfterEach
    void stopCapturing() {
        serviceLogger.detachAppender(logged);
    }

    private List<ILoggingEvent> warningsWithACause() {
        return logged.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .filter(event -> event.getKeyValuePairs() != null
                        && event.getKeyValuePairs().stream().anyMatch(pair -> pair.key.equals("cause")))
                .toList();
    }

    @Test
    void aCorruptImageIsRejectedAndItsCauseIsLogged() throws Exception {
        byte[] corrupt = new byte[3000];
        System.arraycopy(PNG_SIGNATURE, 0, corrupt, 0, PNG_SIGNATURE.length);
        new java.util.Random(1).nextBytes(java.util.Arrays.copyOfRange(corrupt, 8, 3000));
        java.util.Arrays.fill(corrupt, 8, 3000, (byte) 7);

        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.png", "image/png", corrupt)))
                .andExpect(status().isBadRequest());

        assertEquals(1, warningsWithACause().size(), logged.list.toString());
    }

    @Test
    void aCorruptPdfIsRejectedAndItsCauseIsLogged() throws Exception {
        byte[] corrupt = ("%PDF-1.4\n" + "junk junk junk\n".repeat(50)).getBytes();

        mvc.perform(multipart("/detect").file(new MockMultipartFile("file", "a.pdf", "application/pdf", corrupt)))
                .andExpect(status().isBadRequest());

        List<ILoggingEvent> warnings = warningsWithACause();
        assertEquals(1, warnings.size(), logged.list.toString());
        assertTrue(warnings.get(0).getFormattedMessage().contains("pdf"));
    }
}
