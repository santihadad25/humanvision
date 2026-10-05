package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.model.detector.VerticalStroke;
import com.humanvision.checkbox.service.config.DetectionProperties;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class SquareFinder {
    private static final double ALIGNMENT_TOLERANCE_AS_SHARE_OF_HEIGHT = 0.15;
    private static final int MIN_ALIGNMENT_TOLERANCE = 3;
    private static final double TALL_SIDES_AS_SHARE_OF_WIDTH = 1.5;
    private static final double RUNG_ROW_INK_SHARE = 0.85;
    private static final double SIDE_COLUMN_INK_SHARE = 0.2;
    private static final double SIDE_ROWS_SKIPPED_AS_SHARE_OF_HEIGHT = 0.25;

    private record Rung(int firstRow, int lastRow) {}

    private final DetectionProperties properties;

    SquareFinder(DetectionProperties properties) {
        this.properties = properties;
    }

    List<Square> find(BinaryImage image, List<VerticalStroke> strokes) {
        List<VerticalStroke> strokesLeftToRight = strokes.stream().sorted(Comparator.comparingInt(VerticalStroke::left)).toList();
        return IntStream.range(0, strokesLeftToRight.size())
                .boxed()
                .flatMap(leftStrokeIndex -> squaresWithLeftSide(image, strokesLeftToRight, leftStrokeIndex))
                .toList();
    }

    private Stream<Square> squaresWithLeftSide(BinaryImage image, List<VerticalStroke> strokesLeftToRight, int leftStrokeIndex) {
        VerticalStroke leftStroke = strokesLeftToRight.get(leftStrokeIndex);
        return strokesLeftToRight.subList(leftStrokeIndex + 1, strokesLeftToRight.size()).stream()
                .takeWhile(candidate -> candidate.left() - leftStroke.left() <= properties.maxBoxSize())
                .flatMap(rightStroke -> Stream.concat(
                        squareSpanning(leftStroke, rightStroke).stream(),
                        squaresBetweenRungs(image, leftStroke, rightStroke)));
    }

    private List<Square> squareSpanning(VerticalStroke leftStroke, VerticalStroke rightStroke) {
        if (!areAlignedAtTopAndBottom(leftStroke, rightStroke)) {
            return List.of();
        }
        return List.of(new Square(
                leftStroke.left(),
                Math.min(leftStroke.top(), rightStroke.top()),
                rightStroke.right(),
                Math.max(leftStroke.bottom(), rightStroke.bottom())));
    }

    private static boolean areAlignedAtTopAndBottom(VerticalStroke leftStroke, VerticalStroke rightStroke) {
        int tolerance = Math.max(
                MIN_ALIGNMENT_TOLERANCE,
                (int) Math.round(leftStroke.height() * ALIGNMENT_TOLERANCE_AS_SHARE_OF_HEIGHT));
        return Math.abs(leftStroke.top() - rightStroke.top()) <= tolerance
                && Math.abs(leftStroke.bottom() - rightStroke.bottom()) <= tolerance;
    }

    private Stream<Square> squaresBetweenRungs(BinaryImage image, VerticalStroke leftStroke, VerticalStroke rightStroke) {
        int width = rightStroke.right() - leftStroke.left();
        int firstRow = Math.max(leftStroke.top(), rightStroke.top());
        int endRow = Math.min(leftStroke.bottom(), rightStroke.bottom());
        boolean hasRoomForSeveralBoxes = endRow - firstRow >= width * TALL_SIDES_AS_SHARE_OF_WIDTH;
        if (width < properties.minBoxSize() || width > properties.maxBoxSize() || !hasRoomForSeveralBoxes) {
            return Stream.empty();
        }
        List<Rung> rungs = findRungs(image, leftStroke.right(), rightStroke.left(), firstRow, endRow);
        return IntStream.range(0, rungs.size())
                .boxed()
                .flatMap(upperIndex -> rungs.subList(upperIndex + 1, rungs.size()).stream()
                        .takeWhile(lower -> lower.lastRow() + 1 - rungs.get(upperIndex).firstRow() <= properties.maxBoxSize())
                        .filter(lower -> lower.lastRow() + 1 - rungs.get(upperIndex).firstRow() >= properties.minBoxSize())
                        .map(lower -> fittedSquareBetweenRungs(image, leftStroke, rightStroke, rungs.get(upperIndex).firstRow(), lower.lastRow() + 1)));
    }

    private Square fittedSquareBetweenRungs(BinaryImage image, VerticalStroke leftStroke, VerticalStroke rightStroke, int top, int bottom) {
        int skippedRows = (int) Math.round((bottom - top) * SIDE_ROWS_SKIPPED_AS_SHARE_OF_HEIGHT);
        int firstRow = top + skippedRows;
        int endRow = bottom - skippedRows;
        int left = leftStroke.left();
        while (left < leftStroke.right() - 1 && image.inkShareInColumn(left, firstRow, endRow) < SIDE_COLUMN_INK_SHARE) {
            left++;
        }
        int lastColumn = rightStroke.right() - 1;
        while (lastColumn > rightStroke.left() && image.inkShareInColumn(lastColumn, firstRow, endRow) < SIDE_COLUMN_INK_SHARE) {
            lastColumn--;
        }
        return new Square(left, top, lastColumn + 1, bottom);
    }

    private List<Rung> findRungs(BinaryImage image, int fromColumn, int endColumn, int firstRow, int endRow) {
        List<Rung> rungs = new ArrayList<>();
        int rungStart = -1;
        for (int row = firstRow; row < endRow; row++) {
            boolean isRungRow = image.inkShareInRow(row, fromColumn, endColumn) >= RUNG_ROW_INK_SHARE;
            if (isRungRow && rungStart < 0) {
                rungStart = row;
            } else if (!isRungRow && rungStart >= 0) {
                rungs.add(new Rung(rungStart, row - 1));
                rungStart = -1;
            }
        }
        if (rungStart >= 0) {
            rungs.add(new Rung(rungStart, endRow - 1));
        }
        return rungs;
    }
}
