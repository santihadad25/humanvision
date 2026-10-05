package com.humanvision.checkbox.service.detector;

final class OtsuThreshold {
    private static final int GRAY_LEVELS = 256;
    private static final double RELATIVE_TIE_TOLERANCE = 1e-9;

    private OtsuThreshold() {}

    static int of(byte[] grayPixels) {
        long[] pixelsAtLevel = countPixelsAtEachLevel(grayPixels);
        long totalPixels = grayPixels.length;
        long totalLevelSum = sumOfLevels(pixelsAtLevel);

        long darkPixels = 0;
        long darkLevelSum = 0;
        double bestSeparation = -1;
        int firstBestLevel = 0;
        int lastBestLevel = 0;
        for (int level = 0; level < GRAY_LEVELS; level++) {
            darkPixels += pixelsAtLevel[level];
            darkLevelSum += (long) level * pixelsAtLevel[level];
            long lightPixels = totalPixels - darkPixels;
            if (darkPixels == 0 || lightPixels == 0) {
                continue;
            }
            double separation = separationBetweenClasses(
                    darkPixels, darkLevelSum, lightPixels, totalLevelSum - darkLevelSum);
            if (isClearlyAbove(separation, bestSeparation)) {
                bestSeparation = separation;
                firstBestLevel = level;
                lastBestLevel = level;
            } else if (isTied(separation, bestSeparation)) {
                lastBestLevel = level;
            }
        }
        return (firstBestLevel + lastBestLevel) / 2;
    }

    private static long[] countPixelsAtEachLevel(byte[] grayPixels) {
        long[] pixelsAtLevel = new long[GRAY_LEVELS];
        for (byte pixel : grayPixels) {
            pixelsAtLevel[pixel & 0xFF]++;
        }
        return pixelsAtLevel;
    }

    private static long sumOfLevels(long[] pixelsAtLevel) {
        long sum = 0;
        for (int level = 0; level < GRAY_LEVELS; level++) {
            sum += level * pixelsAtLevel[level];
        }
        return sum;
    }

    private static double separationBetweenClasses(
            long darkPixels, long darkLevelSum, long lightPixels, long lightLevelSum) {
        double meanDarkLevel = (double) darkLevelSum / darkPixels;
        double meanLightLevel = (double) lightLevelSum / lightPixels;
        double meanGap = meanDarkLevel - meanLightLevel;
        return (double) darkPixels * lightPixels * meanGap * meanGap;
    }

    private static boolean isClearlyAbove(double separation, double bestSeparation) {
        return separation > bestSeparation * (1 + RELATIVE_TIE_TOLERANCE);
    }

    private static boolean isTied(double separation, double bestSeparation) {
        return separation >= bestSeparation * (1 - RELATIVE_TIE_TOLERANCE);
    }
}
