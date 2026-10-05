package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.InkRun;
import com.humanvision.checkbox.model.detector.VerticalStroke;
import com.humanvision.checkbox.service.config.DetectionProperties;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class VerticalStrokeFinder {
    private static final double SHORTEST_EDGE_AS_SHARE_OF_MIN_BOX = 0.8;
    private static final int MIN_ALLOWED_EDGE_THICKNESS = 3;

    private final int shortestStrokeLength;
    private final int thickestStroke;

    VerticalStrokeFinder(DetectionProperties properties) {
        this.shortestStrokeLength = (int) Math.round(properties.minBoxSize() * SHORTEST_EDGE_AS_SHARE_OF_MIN_BOX);
        this.thickestStroke = Math.max(MIN_ALLOWED_EDGE_THICKNESS, properties.minBoxSize() / 2);
    }

    List<VerticalStroke> find(BinaryImage image) {
        List<InkRun> runs = findLongVerticalInkRuns(image);
        return groupTouchingRuns(runs).stream()
                .map(VerticalStroke::enclosing)
                .filter(this::couldBeSideOfCheckbox)
                .toList();
    }

    private List<InkRun> findLongVerticalInkRuns(BinaryImage image) {
        List<InkRun> runs = new ArrayList<>();
        for (int column = 0; column < image.width(); column++) {
            runs.addAll(findLongInkRunsInColumn(image, column));
        }
        return runs;
    }

    private List<InkRun> findLongInkRunsInColumn(BinaryImage image, int column) {
        List<InkRun> runs = new ArrayList<>();
        int row = 0;
        while (row < image.height()) {
            int runStart = firstInkRowFrom(image, column, row);
            if (runStart == image.height()) {
                break;
            }
            int runEnd = firstPaperRowFrom(image, column, runStart);
            if (runEnd - runStart >= shortestStrokeLength) {
                runs.add(new InkRun(column, runStart, runEnd - 1));
            }
            row = runEnd;
        }
        return runs;
    }

    private static int firstInkRowFrom(BinaryImage image, int column, int fromRow) {
        int row = fromRow;
        while (row < image.height() && !image.isInk(column, row)) {
            row++;
        }
        return row;
    }

    private static int firstPaperRowFrom(BinaryImage image, int column, int fromRow) {
        int row = fromRow;
        while (row < image.height() && image.isInk(column, row)) {
            row++;
        }
        return row;
    }

    private List<List<InkRun>> groupTouchingRuns(List<InkRun> runsOrderedByColumn) {
        DisjointSets runGroups = new DisjointSets(runsOrderedByColumn.size());
        for (int runIndex = 0; runIndex < runsOrderedByColumn.size(); runIndex++) {
            mergeWithTouchingRunsInPreviousColumn(runsOrderedByColumn, runIndex, runGroups);
        }
        return new ArrayList<>(IntStream.range(0, runsOrderedByColumn.size())
                .boxed()
                .collect(Collectors.groupingBy(
                        runGroups::representativeOf,
                        LinkedHashMap::new,
                        Collectors.mapping(runsOrderedByColumn::get, Collectors.toList())))
                .values());
    }

    private static void mergeWithTouchingRunsInPreviousColumn(
            List<InkRun> runsOrderedByColumn, int runIndex, DisjointSets runGroups) {
        InkRun run = runsOrderedByColumn.get(runIndex);
        int previousColumn = run.column() - 1;
        for (int earlier = runIndex - 1; earlier >= 0; earlier--) {
            InkRun earlierRun = runsOrderedByColumn.get(earlier);
            if (earlierRun.column() < previousColumn) {
                break;
            }
            if (earlierRun.column() == previousColumn && run.touches(earlierRun)) {
                runGroups.merge(runIndex, earlier);
            }
        }
    }

    private boolean couldBeSideOfCheckbox(VerticalStroke stroke) {
        return stroke.height() >= shortestStrokeLength && stroke.width() <= thickestStroke;
    }
}
