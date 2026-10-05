package com.humanvision.checkbox.service.detector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

final class PageCanvas {
    private static final int EDGE_THICKNESS = 3;

    private final BufferedImage image;
    private final Graphics2D graphics;

    PageCanvas(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
    }

    PageCanvas box(int left, int top, int size) {
        graphics.fillRect(left, top, size, EDGE_THICKNESS);
        graphics.fillRect(left, top + size - EDGE_THICKNESS, size, EDGE_THICKNESS);
        graphics.fillRect(left, top, EDGE_THICKNESS, size);
        graphics.fillRect(left + size - EDGE_THICKNESS, top, EDGE_THICKNESS, size);
        return this;
    }

    PageCanvas checkedBox(int left, int top, int size) {
        box(left, top, size);
        graphics.setStroke(new BasicStroke(EDGE_THICKNESS));
        int inset = EDGE_THICKNESS + 3;
        graphics.drawLine(left + inset, top + inset, left + size - inset, top + size - inset);
        graphics.drawLine(left + size - inset, top + inset, left + inset, top + size - inset);
        return this;
    }

    PageCanvas filledBlock(int left, int top, int width, int height) {
        graphics.fillRect(left, top, width, height);
        return this;
    }

    PageCanvas glyph(BufferedImage glyphImage, int left, int top) {
        graphics.drawImage(glyphImage, left, top, null);
        return this;
    }

    PageCanvas stackOfBoxesSharingBorders(int left, int top, int size, int count) {
        for (int index = 0; index < count; index++) {
            box(left, top + index * (size - EDGE_THICKNESS), size);
        }
        return this;
    }

    PageCanvas gap(int left, int top, int width, int height) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(left, top, width, height);
        graphics.setColor(Color.BLACK);
        return this;
    }

    PageCanvas thickFrame(int left, int top, int width, int height, int thickness) {
        graphics.fillRect(left, top, width, thickness);
        graphics.fillRect(left, top + height - thickness, width, thickness);
        graphics.fillRect(left, top, thickness, height);
        graphics.fillRect(left + width - thickness, top, thickness, height);
        return this;
    }

    PageCanvas horizontalLine(int top, int fromX, int toX) {
        graphics.fillRect(fromX, top, toX - fromX, EDGE_THICKNESS);
        return this;
    }

    PageCanvas verticalEdge(int left, int top, int height) {
        graphics.fillRect(left, top, EDGE_THICKNESS, height);
        return this;
    }

    byte[] toPng() throws Exception {
        graphics.dispose();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        return bytes.toByteArray();
    }
}
