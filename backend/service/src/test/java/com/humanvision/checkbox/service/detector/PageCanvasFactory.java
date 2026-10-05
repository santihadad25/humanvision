package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.BinaryImage;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class PageCanvasFactory {

    private PageCanvasFactory() {}

    public static BinaryImage pageWithBox(int width, int height, int left, int top, int size) throws Exception {
        return TestPages.binarize(new PageCanvas(width, height).box(left, top, size).toPng());
    }

    public static BinaryImage pageWithTableRowAround(int width, int height, int top, int boxSize) throws Exception {
        return TestPages.binarize(new PageCanvas(width, height)
                .horizontalLine(top, 0, width).horizontalLine(top + boxSize - 3, 0, width)
                .verticalEdge(20, top, boxSize).verticalEdge(20 + boxSize - 3, top, boxSize).toPng());
    }

    public static BinaryImage pageWithoutBottom(int width, int height, int left, int top, int size) throws Exception {
        return TestPages.binarize(new PageCanvas(width, height)
                .horizontalLine(top, left, left + size)
                .verticalEdge(left, top, size).verticalEdge(left + size - 3, top, size).toPng());
    }

    public static BinaryImage pageWithBoldLowercaseO(int width, int height, int left, int top) throws Exception {
        BufferedImage letter = ImageIO.read(PageCanvasFactory.class.getResourceAsStream("/glyphs/bold-lowercase-o.png"));
        return TestPages.binarize(new PageCanvas(width, height).glyph(letter, left, top).toPng());
    }

    public static BinaryImage pageWithBoxAndNeighbourStroke(int width, int height, int left, int top, int size, int gap)
            throws Exception {
        return TestPages.binarize(new PageCanvas(width, height).box(left, top, size)
                .verticalEdge(left + size + gap, top + 5, size - 10).toPng());
    }

    public static BinaryImage pageWithBoxHavingAGap(int width, int height, int left, int top, int size, int gapLeft, int gapTop,
                                                    int gapWidth, int gapHeight) throws Exception {
        return TestPages.binarize(new PageCanvas(width, height).box(left, top, size)
                .gap(gapLeft, gapTop, gapWidth, gapHeight).toPng());
    }

    public static BinaryImage pageWithBoxBetweenNeighbourStrokes(int width, int height, int left, int top, int size, int gap)
            throws Exception {
        return TestPages.binarize(new PageCanvas(width, height).box(left, top, size)
                .verticalEdge(left + size + gap, top + 5, size - 10)
                .verticalEdge(left - gap - 3, top + 5, size - 10).toPng());
    }

    public static BinaryImage pageWithStackOfBoxes(int width, int height, int left, int top, int size, int count)
            throws Exception {
        return TestPages.binarize(new PageCanvas(width, height).stackOfBoxesSharingBorders(left, top, size, count).toPng());
    }
}
