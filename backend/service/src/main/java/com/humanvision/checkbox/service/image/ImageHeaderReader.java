package com.humanvision.checkbox.service.image;

import com.humanvision.checkbox.model.domain.ImageInfo;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.logging.RejectedInputLog;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageHeaderReader {
    private static final Logger log = LoggerFactory.getLogger(ImageHeaderReader.class);

    private ImageHeaderReader() {}

    public static ImageInfo read(byte[] imageBytes) {
        try (ImageInputStream imageStream = new MemoryCacheImageInputStream(new ByteArrayInputStream(imageBytes))) {
            ImageReader reader = findReader(imageStream);
            try {
                reader.setInput(imageStream, true, true);
                return describe(reader);
            } finally {
                reader.dispose();
            }
        } catch (InvalidDocumentException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            RejectedInputLog.warn(log, "image header could not be read", exception);
            throw new InvalidDocumentException("Image is corrupt or could not be decoded.");
        }
    }

    private static ImageReader findReader(ImageInputStream imageStream) {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
        if (!readers.hasNext()) {
            throw new InvalidDocumentException("File is not a supported image.");
        }
        return readers.next();
    }

    private static ImageInfo describe(ImageReader reader) throws IOException {
        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        return new ImageInfo(format, reader.getWidth(0), reader.getHeight(0));
    }
}
