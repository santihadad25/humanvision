package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanvision.checkbox.model.detector.Square;
import java.util.List;
import org.junit.jupiter.api.Test;

class OverlappingSquareRemoverTest {
    private final OverlappingSquareRemover remover = new OverlappingSquareRemover();

    @Test
    void keepsTheSmallerOfTwoOverlappingSquares() {
        Square inner = new Square(10, 10, 50, 50);
        Square outer = new Square(8, 8, 52, 52);

        assertEquals(List.of(inner), remover.removeOverlapping(List.of(outer, inner)));
    }

    @Test
    void keepsSeparateSquares() {
        List<Square> squares = List.of(new Square(0, 0, 40, 40), new Square(100, 0, 140, 40));

        assertEquals(2, remover.removeOverlapping(squares).size());
    }
}
