package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanvision.checkbox.model.detector.VerticalStroke;
import com.humanvision.checkbox.service.config.DetectionProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class VerticalStrokeFinderTest {
    private final VerticalStrokeFinder finder = new VerticalStrokeFinder(new DetectionProperties(20, 100, 1.0, 0.07, 0.4, 3, 0.02));

    private List<VerticalStroke> findIn(PageCanvas canvas) throws Exception {
        return finder.find(TestPages.binarize(canvas.toPng()));
    }

    @Test
    void findsTheTwoSidesOfABoxAsOneEdgeEach() throws Exception {
        List<VerticalStroke> strokes = findIn(new PageCanvas(200, 100).box(50, 20, 40));

        assertEquals(2, strokes.size());
        assertEquals(50, strokes.get(0).left());
        assertEquals(20, strokes.get(0).top());
        assertEquals(40, strokes.get(0).height());
        assertEquals(3, strokes.get(0).width());
    }

    @Test
    void ignoresStrokesShorterThanACheckboxSide() throws Exception {
        assertEquals(List.of(), findIn(new PageCanvas(200, 100).verticalEdge(50, 20, 10)));
    }

    @Test
    void keepsStrokesLongerThanACheckboxSideBecauseStackedBoxesShareThem() throws Exception {
        List<VerticalStroke> strokes = findIn(new PageCanvas(200, 300).verticalEdge(50, 10, 250));

        assertEquals(1, strokes.size());
        assertEquals(250, strokes.get(0).height());
    }

    @Test
    void ignoresThickBlocksOfInk() throws Exception {
        assertEquals(List.of(), findIn(new PageCanvas(200, 100).filledBlock(50, 20, 30, 40)));
    }
}
