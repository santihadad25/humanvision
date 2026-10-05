package com.humanvision.checkbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanvision.checkbox.model.contract.CheckboxDetector;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ResponseContractTest {
    @TestConfiguration
    static class FakeDetector {
        @Bean
        @Primary
        CheckboxDetector detectorWithBoxes() {
            return image -> List.of(new DetectedCheckbox(10, 20, 30, 40, true), new DetectedCheckbox(50, 60, 70, 80, false));
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    static Stream<MockMultipartFile> uploads() throws Exception {
        return Stream.of(
                new MockMultipartFile("file", "a.png", "image/png", image("png")),
                new MockMultipartFile("file", "a.jpg", "image/jpeg", image("jpeg")),
                new MockMultipartFile("file", "a.pdf", "application/pdf", twoPagePdf()));
    }

    private static byte[] image(String format) throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB), format, out);
        return out.toByteArray();
    }

    private static byte[] twoPagePdf() {
        return ("%PDF-1.4\n"
                + "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                + "2 0 obj<</Type/Pages/Kids[3 0 R 4 0 R]/Count 2>>endobj\n"
                + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
                + "4 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
                + "trailer<</Root 1 0 R/Size 5>>\n%%EOF\n").getBytes();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("uploads")
    void challengeResponseHasTheShapeTheChallengeAsks(MockMultipartFile upload) throws Exception {
        String body = mvc.perform(multipart("/detect").file(upload)).andReturn().getResponse().getContentAsString();
        JsonNode root = mapper.readTree(body);

        assertTrue(root.path("boxes").isArray(), "top-level 'boxes' must be an array: " + body);
        assertTrue(root.path("boxes").size() > 0, "fake detector returns boxes: " + body);

        root.get("boxes").forEach(ResponseContractTest::assertMatchesChallengeBox);
    }

    private static void assertMatchesChallengeBox(JsonNode box) {
        JsonNode bbox = box.path("bbox");
        assertTrue(bbox.isArray() && bbox.size() == 4, "bbox must be [x1, y1, x2, y2]: " + box);
        bbox.forEach(coordinate -> assertTrue(coordinate.isInt(), "bbox values must be integer pixels: " + box));
        assertTrue(bbox.get(0).asInt() < bbox.get(2).asInt(), "x1 < x2 (top-left, then bottom-right): " + box);
        assertTrue(bbox.get(1).asInt() < bbox.get(3).asInt(), "y1 < y2 (top-left, then bottom-right): " + box);
        assertTrue(box.path("is_checked").isBoolean(), "is_checked must be a JSON boolean: " + box);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("uploads")
    void challengeEndpointHasExactlyTheFieldsOfTheChallenge(MockMultipartFile upload) throws Exception {
        JsonNode root = mapper.readTree(
                mvc.perform(multipart("/detect").file(upload)).andReturn().getResponse().getContentAsString());

        assertEquals(List.of("boxes"), fieldNames(root));
        assertEquals(List.of("bbox", "is_checked"), fieldNames(root.get("boxes").get(0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("uploads")
    void pagedEndpointAddsThePageOfEachBoxAndTheSizeOfEveryPage(MockMultipartFile upload) throws Exception {
        JsonNode root = mapper.readTree(
                mvc.perform(multipart("/v2/detect").file(upload)).andReturn().getResponse().getContentAsString());

        assertEquals(List.of("boxes", "pages"), fieldNames(root));
        assertEquals(List.of("page", "bbox", "is_checked"), fieldNames(root.get("boxes").get(0)));
        assertEquals(List.of("page", "width", "height"), fieldNames(root.get("pages").get(0)));
    }

    @Test
    void challengeEndpointJoinsTheBoxesOfTheTwoPagesOfAPdf() throws Exception {
        MockMultipartFile pdf = uploads().toList().get(2);

        JsonNode challenge = mapper.readTree(
                mvc.perform(multipart("/detect").file(pdf)).andReturn().getResponse().getContentAsString());
        JsonNode paged = mapper.readTree(
                mvc.perform(multipart("/v2/detect").file(pdf)).andReturn().getResponse().getContentAsString());

        assertEquals(4, challenge.get("boxes").size());
        assertEquals(List.of(1, 1, 2, 2), paged.get("boxes").findValues("page").stream().map(JsonNode::asInt).toList());
    }

    private static List<String> fieldNames(JsonNode node) {
        return node.properties().stream().map(Map.Entry::getKey).toList();
    }
}
