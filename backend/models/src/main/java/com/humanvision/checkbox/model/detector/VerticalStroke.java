package com.humanvision.checkbox.model.detector;

import java.util.List;

public record VerticalStroke(int left, int top, int width, int height) {
    public static VerticalStroke enclosing(List<InkRun> runs) {
        int firstColumn = runs.stream().mapToInt(InkRun::column).min().orElseThrow();
        int lastColumn = runs.stream().mapToInt(InkRun::column).max().orElseThrow();
        int firstRow = runs.stream().mapToInt(InkRun::firstRow).min().orElseThrow();
        int lastRow = runs.stream().mapToInt(InkRun::lastRow).max().orElseThrow();
        return new VerticalStroke(firstColumn, firstRow, lastColumn - firstColumn + 1, lastRow - firstRow + 1);
    }

    public int right() {
        return left + width;
    }

    public int bottom() {
        return top + height;
    }
}
