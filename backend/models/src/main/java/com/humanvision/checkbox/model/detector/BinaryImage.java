package com.humanvision.checkbox.model.detector;

public final class BinaryImage {
    private final boolean[] isInkAtPixel;
    private final int width;
    private final int height;

    public BinaryImage(boolean[] isInkAtPixel, int width, int height) {
        if (isInkAtPixel.length != width * height) {
            throw new IllegalArgumentException("Expected " + width * height + " pixels but got " + isInkAtPixel.length);
        }
        this.isInkAtPixel = isInkAtPixel;
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean isInk(int column, int row) {
        return isInkAtPixel[row * width + column];
    }

    public boolean hasInkInColumn(int column, int firstRow, int endRow) {
        for (int row = firstRow; row < endRow; row++) {
            if (isInk(column, row)) {
                return true;
            }
        }
        return false;
    }

    public double inkShareInColumn(int column, int firstRow, int endRow) {
        if (endRow <= firstRow) {
            return 0;
        }
        int inkPixels = 0;
        for (int row = firstRow; row < endRow; row++) {
            if (isInk(column, row)) {
                inkPixels++;
            }
        }
        return (double) inkPixels / (endRow - firstRow);
    }

    public boolean hasInkInRow(int row, int firstColumn, int endColumn) {
        for (int column = firstColumn; column < endColumn; column++) {
            if (isInk(column, row)) {
                return true;
            }
        }
        return false;
    }

    public double inkShareInRow(int row, int firstColumn, int endColumn) {
        if (endColumn <= firstColumn) {
            return 0;
        }
        int inkPixels = 0;
        for (int column = firstColumn; column < endColumn; column++) {
            if (isInk(column, row)) {
                inkPixels++;
            }
        }
        return (double) inkPixels / (endColumn - firstColumn);
    }

    public double inkShareWithin(Square region) {
        long inkPixels = 0;
        for (int row = region.top(); row < region.bottom(); row++) {
            for (int column = region.left(); column < region.right(); column++) {
                if (isInk(column, row)) {
                    inkPixels++;
                }
            }
        }
        return (double) inkPixels / region.area();
    }
}
