package com.humanvision.checkbox.service.validator;

import com.humanvision.checkbox.model.contract.FileValidator;
import com.humanvision.checkbox.model.domain.FileType;
import com.humanvision.checkbox.model.domain.ImageInfo;
import com.humanvision.checkbox.model.domain.InvalidDocumentException;
import com.humanvision.checkbox.service.image.ImageHeaderReader;
import com.humanvision.checkbox.service.config.ImageLimits;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ImageFileValidator implements FileValidator {
    private static final Logger log = LoggerFactory.getLogger(ImageFileValidator.class);

    private final ImageLimits imageLimits;

    public ImageFileValidator(ImageLimits imageLimits) {
        this.imageLimits = imageLimits;
    }

    @Override
    public Set<FileType> supportedTypes() {
        return Set.of(FileType.PNG, FileType.JPEG);
    }

    @Override
    public void validate(byte[] file) {
        validateNotEmpty(file);

        ImageInfo imageInfo = ImageHeaderReader.read(file);

        validateDecodedFormatMatchesSignature(file, imageInfo);
        validateDimensions(imageInfo.width(), imageInfo.height());

        log.atInfo()
                .addKeyValue("format", imageInfo.format())
                .addKeyValue("width", imageInfo.width())
                .addKeyValue("height", imageInfo.height())
                .log("image validated");
    }

    private static void validateNotEmpty(byte[] file) {
        if (file == null || file.length == 0) {
            throw new InvalidDocumentException("Image is empty.");
        }
    }

    private static void validateDecodedFormatMatchesSignature(byte[] file, ImageInfo imageInfo) {
        boolean decoderAgreesWithSignature = FileType.detect(file)
                .map(FileType::label)
                .filter(imageInfo.format()::equals)
                .isPresent();
        if (!decoderAgreesWithSignature) {
            throw new InvalidDocumentException("Image content does not match its file type.");
        }
    }

    private void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new InvalidDocumentException("Image dimensions are invalid.");
        }
        if (width > imageLimits.maxDimension() || height > imageLimits.maxDimension()) {
            throw new InvalidDocumentException("Image dimensions exceed the allowed limit.");
        }
        if ((long) width * height > imageLimits.maxPixels()) {
            throw new InvalidDocumentException("Image contains too many pixels.");
        }
    }
}
