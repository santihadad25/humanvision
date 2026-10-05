package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.logging.RejectedInputLog;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PageDecoder {
    private static final Logger log = LoggerFactory.getLogger(PageDecoder.class);

    GrayPage decode(byte[] encodedImage) {
        BufferedImage decoded = readImageOrReject(encodedImage);
        BufferedImage grayImage = new BufferedImage(decoded.getWidth(), decoded.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D canvas = grayImage.createGraphics();
        try {
            canvas.setColor(Color.WHITE);
            canvas.fillRect(0, 0, grayImage.getWidth(), grayImage.getHeight());
            canvas.drawImage(decoded, 0, 0, null);
        } finally {
            canvas.dispose();
        }
        byte[] pixels = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();
        return new GrayPage(pixels, grayImage.getWidth(), grayImage.getHeight());
    }

    private static BufferedImage readImageOrReject(byte[] encodedImage) {
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(encodedImage));
        } catch (IOException | RuntimeException exception) {
            RejectedInputLog.warn(log, "image could not be decoded", exception);
            throw new InvalidDocumentException("Image could not be decoded.");
        }
        if (image == null) {
            throw new InvalidDocumentException("Image could not be decoded.");
        }
        return image;
    }
}
