package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanvision.checkbox.model.detector.Square;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DominantSizeFilterTest {
    private final DominantSizeFilter filter = new DominantSizeFilter();

    private static Square squareOfSize(int size) {
        return new Square(0, 0, size, size);
    }

    @Test
    void dropsIsolatedSquaresOfAnotherSize() {
        List<Square> squares = Stream.of(52, 52, 53, 51, 52, 52, 52, 52, 52, 52, 27).map(DominantSizeFilterTest::squareOfSize).toList();

        List<Square> kept = filter.keepDominantSizes(squares);

        assertEquals(10, kept.size());
        assertEquals(0, kept.stream().filter(square -> square.averageSide() == 27).count());
    }

    @Test
    void keepsASecondSizeWhenItIsCommonEnough() {
        List<Square> squares = Stream.of(30, 30, 30, 30, 52, 52, 52, 52, 52, 52).map(DominantSizeFilterTest::squareOfSize).toList();

        assertEquals(10, filter.keepDominantSizes(squares).size());
    }

    @Test
    void dropsALoneOddSquareOnceFourConsistentBoxesExist() {
        List<Square> squares = Stream.of(40, 40, 40, 40, 90).map(DominantSizeFilterTest::squareOfSize).toList();

        assertEquals(4, filter.keepDominantSizes(squares).size());
    }

    @Test
    void doesNotJudgeOutliersWhenThereAreTooFewBoxes() {
        List<Square> squares = Stream.of(40, 40, 90).map(DominantSizeFilterTest::squareOfSize).toList();

        assertEquals(3, filter.keepDominantSizes(squares).size());
    }

    @Test
    void keepsTheFewBoxesOfASmallDocument() {
        assertEquals(2, filter.keepDominantSizes(List.of(squareOfSize(40), squareOfSize(40))).size());
        assertEquals(List.of(), filter.keepDominantSizes(List.of()));
    }
}
