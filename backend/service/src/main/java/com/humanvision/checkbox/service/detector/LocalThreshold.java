package com.humanvision.checkbox.service.detector;

final class LocalThreshold {
    private LocalThreshold() {}

    static boolean[] inkPixels(byte[] gray, int width, int height, int window, int margin) {
        int half = window / 2;
        int bufferedRows = window + 1;
        int[][] rowSums = new int[bufferedRows][width];
        int[] columnSums = new int[width];
        boolean[] isInk = new boolean[gray.length];

        int lastRowAdded = -1;
        for (int row = 0; row < height; row++) {
            int bottom = Math.min(height - 1, row + half);
            while (lastRowAdded < bottom) {
                lastRowAdded++;
                int[] sums = rowSums[lastRowAdded % bufferedRows];
                horizontalSums(gray, width, lastRowAdded, half, sums);
                for (int column = 0; column < width; column++) {
                    columnSums[column] += sums[column];
                }
            }
            int leaving = row - half - 1;
            if (leaving >= 0) {
                int[] sums = rowSums[leaving % bufferedRows];
                for (int column = 0; column < width; column++) {
                    columnSums[column] -= sums[column];
                }
            }
            int rows = bottom - Math.max(0, row - half) + 1;
            for (int column = 0; column < width; column++) {
                int columns = Math.min(width - 1, column + half) - Math.max(0, column - half) + 1;
                int average = columnSums[column] / (rows * columns);
                isInk[row * width + column] = (gray[row * width + column] & 0xFF) < average - margin;
            }
        }
        return isInk;
    }

    private static void horizontalSums(byte[] gray, int width, int row, int half, int[] sums) {
        int offset = row * width;
        int sum = 0;
        for (int column = 0; column < Math.min(width, half + 1); column++) {
            sum += gray[offset + column] & 0xFF;
        }
        for (int column = 0; column < width; column++) {
            sums[column] = sum;
            int entering = column + half + 1;
            if (entering < width) {
                sum += gray[offset + entering] & 0xFF;
            }
            int leaving = column - half;
            if (leaving >= 0) {
                sum -= gray[offset + leaving] & 0xFF;
            }
        }
    }
}
