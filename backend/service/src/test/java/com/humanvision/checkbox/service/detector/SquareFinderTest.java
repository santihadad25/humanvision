package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.model.detector.VerticalStroke;
import java.util.List;
import org.junit.jupiter.api.Test;

class SquareFinderTest {
    private static final BinaryImage BLANK_PAGE = new BinaryImage(new boolean[300 * 300], 300, 300);

    private final SquareFinder finder = new SquareFinder(StandardDetector.PROPERTIES);

    @Test
    void pairsAlignedSidesIntoASquareSpanningBoth() {
        List<VerticalStroke> strokes = List.of(new VerticalStroke(10, 10, 3, 40), new VerticalStroke(47, 10, 3, 40));

        assertEquals(List.of(new Square(10, 10, 50, 50)), finder.find(BLANK_PAGE, strokes));
    }

    @Test
    void doesNotPairSidesThatAreNotAlignedVertically() {
        List<VerticalStroke> strokes = List.of(new VerticalStroke(10, 10, 3, 40), new VerticalStroke(47, 40, 3, 40));

        assertEquals(List.of(), finder.find(BLANK_PAGE, strokes));
    }

    @Test
    void doesNotPairSidesFartherApartThanTheBiggestBox() {
        List<VerticalStroke> strokes = List.of(new VerticalStroke(10, 10, 3, 40), new VerticalStroke(200, 10, 3, 40));

        assertEquals(List.of(), finder.find(BLANK_PAGE, strokes));
    }

    @Test
    void pairsEveryLeftSideWithEveryAlignedSideWithinReach() {
        List<VerticalStroke> strokes = List.of(
                new VerticalStroke(10, 10, 3, 40), new VerticalStroke(50, 10, 3, 40), new VerticalStroke(90, 10, 3, 40));

        assertEquals(3, finder.find(BLANK_PAGE, strokes).size());
    }

    @Test
    void cutsTallSidesAtTheRungsBetweenBoxesStackedSharingTheirBorders() throws Exception {
        BinaryImage page = PageCanvasFactory.pageWithStackOfBoxes(120, 200, 20, 20, 40, 3);
        List<VerticalStroke> strokes = new VerticalStrokeFinder(StandardDetector.PROPERTIES).find(page);

        List<Square> squares = finder.find(page, strokes);

        assertEquals(true, squares.contains(new Square(20, 20, 60, 60)));
        assertEquals(true, squares.contains(new Square(20, 57, 60, 97)));
        assertEquals(true, squares.contains(new Square(20, 94, 60, 134)));
    }
}
