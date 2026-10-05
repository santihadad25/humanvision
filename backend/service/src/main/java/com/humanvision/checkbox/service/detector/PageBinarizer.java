package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.service.config.Binarization;
import com.humanvision.checkbox.service.config.PreprocessingProperties;

final class PageBinarizer {
    private final Binarization mode;
    private final int localWindow;
    private final int localMargin;

    PageBinarizer(PreprocessingProperties properties) {
        this.mode = properties.binarization();
        this.localWindow = properties.localWindow();
        this.localMargin = properties.localDelta();
    }

    BinaryImage binarize(GrayPage page) {
        boolean[] isInk = mode == Binarization.LOCAL ? localInk(page) : globalInk(page);
        if (mode == Binarization.BOTH) {
            boolean[] local = localInk(page);
            for (int pixel = 0; pixel < isInk.length; pixel++) {
                isInk[pixel] |= local[pixel];
            }
        }
        return new BinaryImage(isInk, page.width(), page.height());
    }

    private static boolean[] globalInk(GrayPage page) {
        int inkThreshold = OtsuThreshold.of(page.pixels());
        boolean[] isInk = new boolean[page.pixels().length];
        for (int pixel = 0; pixel < isInk.length; pixel++) {
            isInk[pixel] = (page.pixels()[pixel] & 0xFF) <= inkThreshold;
        }
        return isInk;
    }

    private boolean[] localInk(GrayPage page) {
        return LocalThreshold.inkPixels(page.pixels(), page.width(), page.height(), localWindow, localMargin);
    }
}
