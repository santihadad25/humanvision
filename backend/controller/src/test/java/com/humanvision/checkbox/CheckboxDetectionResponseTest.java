package com.humanvision.checkbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.humanvision.checkbox.model.contract.CheckboxDetectionService;
import com.humanvision.checkbox.model.domain.CheckboxDetectionResult;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.model.domain.PageDetection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CheckboxDetectionResponseTest {
    @Autowired MockMvc mvc;
    @MockBean CheckboxDetectionService checkboxDetectionService;

    private final MockMultipartFile upload = new MockMultipartFile("file", "a.png", "image/png", new byte[] {1});

    private void givenOnePageWithOneCheckedBox() {
        when(checkboxDetectionService.detectCheckboxes(any())).thenReturn(
                new CheckboxDetectionResult(List.of(
                        new PageDetection(2, 100, 50, List.of(new DetectedCheckbox(1, 2, 3, 4, true))))));
    }

    @Test
    void challengeEndpointSerializesBoxesWithSnakeCaseNames() throws Exception {
        givenOnePageWithOneCheckedBox();

        mvc.perform(multipart("/detect").file(upload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxes[0].bbox[0]").value(1))
                .andExpect(jsonPath("$.boxes[0].bbox[3]").value(4))
                .andExpect(jsonPath("$.boxes[0].is_checked").value(true))
                .andExpect(jsonPath("$.boxes[0].page").doesNotExist())
                .andExpect(jsonPath("$.pages").doesNotExist());
    }

    @Test
    void pagedEndpointAlsoReturnsPageNumbersAndPageSizes() throws Exception {
        givenOnePageWithOneCheckedBox();

        mvc.perform(multipart("/v2/detect").file(upload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxes[0].page").value(2))
                .andExpect(jsonPath("$.boxes[0].bbox[0]").value(1))
                .andExpect(jsonPath("$.boxes[0].is_checked").value(true))
                .andExpect(jsonPath("$.pages[0].page").value(2))
                .andExpect(jsonPath("$.pages[0].width").value(100))
                .andExpect(jsonPath("$.pages[0].height").value(50));
    }

    @Test
    void unexpectedErrorIs500WithoutLeakingDetails() throws Exception {
        when(checkboxDetectionService.detectCheckboxes(any())).thenThrow(new IllegalStateException("secret internals"));

        mvc.perform(multipart("/detect").file(upload))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Unexpected error."));
    }
}
