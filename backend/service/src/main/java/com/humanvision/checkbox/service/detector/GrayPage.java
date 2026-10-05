package com.humanvision.checkbox.service.detector;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

record GrayPage(byte[] pixels, int width, int height) {

    long pixelCountWhenScaledBy(int factor) {
        return (long) width * factor * height * factor;
    }

    GrayPage scaledBy(int factor) {
        if (factor == 1) {
            return this;
        }
        BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        System.arraycopy(pixels, 0, ((DataBufferByte) source.getRaster().getDataBuffer()).getData(), 0, pixels.length);
        BufferedImage scaled = new BufferedImage(width * factor, height * factor, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D canvas = scaled.createGraphics();
        try {
            canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            canvas.drawImage(source, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        } finally {
            canvas.dispose();
        }
        return new GrayPage(((DataBufferByte) scaled.getRaster().getDataBuffer()).getData(), scaled.getWidth(), scaled.getHeight());
    }
}
