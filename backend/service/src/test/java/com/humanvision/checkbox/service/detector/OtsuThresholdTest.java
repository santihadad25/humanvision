package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class OtsuThresholdTest {

    @Test
    void sitsInTheMiddleOfAPurelyBlackAndWhiteImage() {
        byte[] grayPixels = new byte[1000];
        Arrays.fill(grayPixels, 0, 100, (byte) 0);
        Arrays.fill(grayPixels, 100, 1000, (byte) 255);

        int threshold = OtsuThreshold.of(grayPixels);

        assertTrue(threshold > 100 && threshold < 155, "threshold was " + threshold);
    }

    @Test
    void separatesTwoGrayClustersWithoutExtremeValues() {
        byte[] grayPixels = new byte[1000];
        Arrays.fill(grayPixels, 0, 200, (byte) 60);
        Arrays.fill(grayPixels, 200, 1000, (byte) 200);

        int threshold = OtsuThreshold.of(grayPixels);

        assertTrue(threshold >= 60 && threshold < 200, "threshold was " + threshold);
    }
}
