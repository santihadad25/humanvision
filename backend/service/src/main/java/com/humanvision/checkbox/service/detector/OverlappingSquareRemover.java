package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.Square;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class OverlappingSquareRemover {
    private static final double OVERLAP_RATIO_THAT_MEANS_DUPLICATE = 0.4;

    List<Square> removeOverlapping(List<Square> squares) {
        List<Square> keptSquares = new ArrayList<>();
        for (Square square : smallestFirst(squares)) {
            if (!duplicatesAnyOf(keptSquares, square)) {
                keptSquares.add(square);
            }
        }
        return keptSquares;
    }

    private static List<Square> smallestFirst(List<Square> squares) {
        return squares.stream().sorted(Comparator.comparingLong(Square::area)).toList();
    }

    private static boolean duplicatesAnyOf(List<Square> keptSquares, Square square) {
        return keptSquares.stream()
                .anyMatch(kept -> square.overlapRatioWith(kept) >= OVERLAP_RATIO_THAT_MEANS_DUPLICATE);
    }
}
