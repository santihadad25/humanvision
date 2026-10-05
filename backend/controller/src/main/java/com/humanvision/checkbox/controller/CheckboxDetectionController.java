package com.humanvision.checkbox.controller;

import com.humanvision.checkbox.model.contract.CheckboxDetectionService;
import com.humanvision.checkbox.model.domain.CheckboxDetectionResult;
import com.humanvision.checkbox.model.dto.DetectionResponse;
import com.humanvision.checkbox.model.dto.PagedDetectionResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CheckboxDetectionController {
    private static final Logger log = LoggerFactory.getLogger(CheckboxDetectionController.class);

    private final CheckboxDetectionService checkboxDetectionService;

    public CheckboxDetectionController(CheckboxDetectionService checkboxDetectionService) {
        this.checkboxDetectionService = checkboxDetectionService;
    }

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DetectionResponse detectCheckboxes(@RequestParam("file") MultipartFile file) throws IOException {
        return DetectionResponse.from(detect(file));
    }

    @PostMapping(value = "/v2/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PagedDetectionResponse detectCheckboxesByPage(@RequestParam("file") MultipartFile file) throws IOException {
        return PagedDetectionResponse.from(detect(file));
    }

    private CheckboxDetectionResult detect(MultipartFile file) throws IOException {
        log.atInfo()
                .addKeyValue("size_bytes", file.getSize())
                .addKeyValue("declared_content_type", file.getContentType())
                .log("detection requested");
        return checkboxDetectionService.detectCheckboxes(file.getBytes());
    }
}
