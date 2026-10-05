package com.humanvision.checkbox.model.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SquareTest {

    @Test
    void identicalSquaresOverlapCompletely() {
        Square square = new Square(0, 0, 40, 40);

        assertEquals(1.0, square.overlapRatioWith(square));
    }

    @Test
    void separateSquaresDoNotOverlap() {
        assertEquals(0.0, new Square(0, 0, 40, 40).overlapRatioWith(new Square(100, 0, 140, 40)));
    }

    @Test
    void partiallyOverlappingSquaresShareTheirIntersectionOverTheirUnion() {
        Square first = new Square(0, 0, 40, 40);
        Square second = new Square(20, 0, 60, 40);

        assertEquals(800.0 / 2400.0, first.overlapRatioWith(second), 1e-9);
    }

    @Test
    void shrinkingMovesEverySideInward() {
        assertEquals(Optional.of(new Square(10, 10, 30, 30)), new Square(0, 0, 40, 40).shrunkBy(10));
    }

    @Test
    void shrinkingAwayTheWholeSquareLeavesNothing() {
        assertEquals(Optional.empty(), new Square(0, 0, 40, 40).shrunkBy(20));
        assertEquals(Optional.empty(), new Square(0, 0, 40, 40).shrunkBy(25));
    }

    @Test
    void cornersAreTheFourBlocksAtTheCornersOfTheSquare() {
        assertEquals(
                List.of(new Square(0, 0, 3, 3), new Square(37, 0, 40, 3), new Square(0, 27, 3, 30), new Square(37, 27, 40, 30)),
                new Square(0, 0, 40, 30).corners(3));
    }
}
