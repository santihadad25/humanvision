package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.contract.SquareValidator;
import com.humanvision.checkbox.model.detector.BinaryImage;
import com.humanvision.checkbox.model.detector.Square;
import com.humanvision.checkbox.model.detector.VerticalStroke;
import com.humanvision.checkbox.model.domain.DetectedCheckbox;
import com.humanvision.checkbox.service.config.DetectionProperties;
import com.humanvision.checkbox.service.config.PreprocessingProperties;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SingleScaleDetector {
    private static final Logger log = LoggerFactory.getLogger(SingleScaleDetector.class);
    private static final Comparator<Square> READING_ORDER =
            Comparator.comparingInt(Square::top).thenComparingInt(Square::left);

    private final PageBinarizer binarizer;
    private final VerticalStrokeFinder strokeFinder;
    private final SquareFinder squareFinder;
    private final List<SquareValidator> squareValidators;
    private final OverlappingSquareRemover duplicateRemover = new OverlappingSquareRemover();
    private final DominantSizeFilter sizeFilter = new DominantSizeFilter();
    private final CheckStateClassifier stateClassifier;

    SingleScaleDetector(DetectionProperties properties, PreprocessingProperties preprocessing,
                        List<SquareValidator> squareValidators) {
        if (squareValidators.isEmpty()) {
            throw new IllegalStateException("At least one SquareValidator is needed, or every candidate would be accepted.");
        }
        this.binarizer = new PageBinarizer(preprocessing);
        this.strokeFinder = new VerticalStrokeFinder(properties);
        this.squareFinder = new SquareFinder(properties);
        this.squareValidators = squareValidators;
        this.stateClassifier = new CheckStateClassifier(properties);
    }

    List<DetectedCheckbox> detect(GrayPage scaledPage, int factor) {
        BinaryImage page = binarizer.binarize(scaledPage);
        List<Square> candidates = findCandidateSquares(page);
        List<Square> validSquares = keepValidSquares(page, candidates);
        List<Square> checkboxes = keepConsistentSquares(validSquares);
        log.atDebug()
                .addKeyValue("scale", factor)
                .addKeyValue("candidates", candidates.size())
                .addKeyValue("valid", validSquares.size())
                .addKeyValue("checkboxes", checkboxes.size())
                .log("checkbox candidates filtered");
        return checkboxes.stream()
                .sorted(READING_ORDER)
                .map(checkbox -> toDetectedCheckbox(page, checkbox, factor))
                .toList();
    }

    private List<Square> findCandidateSquares(BinaryImage page) {
        List<VerticalStroke> strokes = strokeFinder.find(page);
        return squareFinder.find(page, strokes);
    }

    private List<Square> keepValidSquares(BinaryImage page, List<Square> candidates) {
        return candidates.stream()
                .filter(square -> squareValidators.stream().allMatch(validator -> validator.isValid(page, square)))
                .toList();
    }

    private List<Square> keepConsistentSquares(List<Square> validSquares) {
        List<Square> distinctSquares = duplicateRemover.removeOverlapping(validSquares);
        return sizeFilter.keepDominantSizes(distinctSquares);
    }

    private DetectedCheckbox toDetectedCheckbox(BinaryImage page, Square checkbox, int factor) {
        boolean isChecked = stateClassifier.isChecked(page, checkbox);
        return new DetectedCheckbox(
                checkbox.left() / factor,
                checkbox.top() / factor,
                ceilingDivide(checkbox.right(), factor),
                ceilingDivide(checkbox.bottom(), factor),
                isChecked);
    }

    private static int ceilingDivide(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }
}
