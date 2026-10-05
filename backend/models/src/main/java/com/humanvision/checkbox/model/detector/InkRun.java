package com.humanvision.checkbox.model.detector;

public record InkRun(int column, int firstRow, int lastRow) {
    public boolean touches(InkRun runInNeighbouringColumn) {
        return firstRow <= runInNeighbouringColumn.lastRow + 1 && runInNeighbouringColumn.firstRow <= lastRow + 1;
    }
}
