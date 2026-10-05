package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.config.PreprocessingProperties;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClassicCheckboxDetectorTest {
    private static final int BOX = 40;
    private static final int POSITION_TOLERANCE = 2;

    private final ClassicCheckboxDetector detector =
            StandardDetector.create();

    private static void assertBoxAt(DetectedCheckbox actual, int left, int top, int size, boolean checked) {
        assertEquals(left, actual.x1(), POSITION_TOLERANCE);
        assertEquals(top, actual.y1(), POSITION_TOLERANCE);
        assertEquals(left + size, actual.x2(), POSITION_TOLERANCE);
        assertEquals(top + size, actual.y2(), POSITION_TOLERANCE);
        assertEquals(checked, actual.checked());
    }

    @Test
    void neverSkipsTheOriginalScaleEvenWhenThePageExceedsTheWorkingPixelLimit() throws Exception {
        PreprocessingProperties tinyLimit = new PreprocessingProperties(
                StandardDetector.PREPROCESSING.binarization(), StandardDetector.PREPROCESSING.localWindow(),
                StandardDetector.PREPROCESSING.localDelta(), List.of(1, 2), 5, 100L);
        byte[] page = new PageCanvas(400, 200).box(50, 50, BOX).toPng();

        List<DetectedCheckbox> boxes = StandardDetector.create(tinyLimit).detect(page);

        assertEquals(1, boxes.size());
    }

    @Test
    void findsEmptyAndCheckedBoxesWithTheirPosition() throws Exception {
        byte[] page = new PageCanvas(400, 200).box(50, 50, BOX).checkedBox(150, 50, BOX).toPng();

        List<DetectedCheckbox> boxes = detector.detect(page);

        assertEquals(2, boxes.size());
        assertBoxAt(boxes.get(0), 50, 50, BOX, false);
        assertBoxAt(boxes.get(1), 150, 50, BOX, true);
    }

    @Test
    void returnsBoxesInReadingOrder() throws Exception {
        byte[] page = new PageCanvas(400, 300).box(200, 200, BOX).box(50, 50, BOX).box(200, 50, BOX).toPng();

        List<DetectedCheckbox> boxes = detector.detect(page);

        assertEquals(List.of(50, 200, 200), boxes.stream().map(DetectedCheckbox::x1).map(x -> Math.round(x / 50f) * 50).toList());
        assertTrue(boxes.get(2).y1() > boxes.get(0).y1());
    }

    @Test
    void findsABoxWhoseTopAndBottomAreTableLines() throws Exception {
        byte[] page = new PageCanvas(400, 200)
                .horizontalLine(100, 0, 400)
                .horizontalLine(137, 0, 400)
                .verticalEdge(50, 100, BOX)
                .verticalEdge(87, 100, BOX)
                .toPng();

        List<DetectedCheckbox> boxes = detector.detect(page);

        assertEquals(1, boxes.size());
        assertEquals(50, boxes.get(0).x1(), POSITION_TOLERANCE);
        assertEquals(100, boxes.get(0).y1(), POSITION_TOLERANCE);
    }

    @Test
    void findsBoxesStackedWithGapsBetweenThem() throws Exception {
        PageCanvas canvas = new PageCanvas(300, 400);
        for (int index = 0; index < 5; index++) {
            canvas.box(50, 20 + index * 45, BOX);
        }

        assertEquals(5, detector.detect(canvas.toPng()).size());
    }

    @Test
    void findsBoxesStackedSharingTheirBorders() throws Exception {
        byte[] page = new PageCanvas(300, 200).stackOfBoxesSharingBorders(50, 20, BOX, 3).toPng();

        assertEquals(3, detector.detect(page).size());
    }

    @Test
    void findsTheThinBoxBelowAThickFrameDrawnAroundAnotherBox() throws Exception {
        byte[] page = new PageCanvas(200, 200)
                .thickFrame(20, 20, 52, 42, 6)
                .box(27, 56, 38)
                .toPng();

        List<DetectedCheckbox> boxes = detector.detect(page);

        assertTrue(boxes.stream().anyMatch(box -> Math.abs(box.x1() - 27) <= 3 && Math.abs(box.x2() - 65) <= 3
                && box.y1() >= 50 && box.y2() >= 90), "the box below the frame was not found: " + boxes);
    }

    @Test
    void ignoresLetterSizedSquares() throws Exception {
        PageCanvas canvas = new PageCanvas(600, 200);
        for (int index = 0; index < 5; index++) {
            canvas.box(30 + index * 70, 30, BOX);
        }
        byte[] page = canvas.box(30, 120, 16).toPng();

        assertEquals(5, detector.detect(page).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/glyphs/bold-lowercase-o.png", "/glyphs/bold-capital-o.png"})
    void ignoresBoldRoundLettersOnAPageWithoutCheckboxes(String glyphResource) throws Exception {
        BufferedImage glyph = ImageIO.read(getClass().getResourceAsStream(glyphResource));
        PageCanvas canvas = new PageCanvas(700, 200);
        for (int index = 0; index < 8; index++) {
            canvas.glyph(glyph, 30 + index * 80, 80);
        }

        assertEquals(List.of(), detector.detect(canvas.toPng()));
    }

    @Test
    void ignoresASquareMuchLargerThanTheOthers() throws Exception {
        PageCanvas canvas = new PageCanvas(700, 300);
        for (int index = 0; index < 5; index++) {
            canvas.box(30 + index * 70, 30, BOX);
        }
        byte[] page = canvas.box(30, 120, 90).toPng();

        assertEquals(5, detector.detect(page).size());
    }

    @Test
    void ignoresRectanglesThatAreNotSquare() throws Exception {
        byte[] page = new PageCanvas(400, 200).box(50, 50, BOX).toPng();
        PageCanvas wide = new PageCanvas(400, 200).verticalEdge(50, 50, 30).verticalEdge(130, 50, 30);

        assertEquals(1, detector.detect(page).size());
        assertEquals(0, detector.detect(wide.horizontalLine(50, 50, 133).horizontalLine(77, 50, 133).toPng()).size());
    }

    @Test
    void blankPageHasNoBoxes() throws Exception {
        assertEquals(List.of(), detector.detect(new PageCanvas(300, 200).toPng()));
    }

    @Test
    void imageSmallerThanAnyBoxIsHandled() throws Exception {
        assertEquals(List.of(), detector.detect(new PageCanvas(5, 5).toPng()));
    }

    @Test
    void undecodableBytesAreRejected() {
        assertThrows(InvalidDocumentException.class, () -> detector.detect("not an image".getBytes()));
    }
}
