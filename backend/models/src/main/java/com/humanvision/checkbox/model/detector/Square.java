package com.humanvision.checkbox.model.detector;

import java.util.List;
import java.util.Optional;

public record Square(int left, int top, int right, int bottom) {
    public int width() {
        return right - left;
    }

    public int height() {
        return bottom - top;
    }

    public int averageSide() {
        return (width() + height()) / 2;
    }

    public long area() {
        return (long) width() * height();
    }

    public double overlapRatioWith(Square other) {
        long overlapWidth = Math.max(0, Math.min(right, other.right) - Math.max(left, other.left));
        long overlapHeight = Math.max(0, Math.min(bottom, other.bottom) - Math.max(top, other.top));
        long intersectionArea = overlapWidth * overlapHeight;
        long unionArea = area() + other.area() - intersectionArea;
        return unionArea == 0 ? 0 : (double) intersectionArea / unionArea;
    }

    public List<Square> corners(int cornerSize) {
        return List.of(
                new Square(left, top, left + cornerSize, top + cornerSize),
                new Square(right - cornerSize, top, right, top + cornerSize),
                new Square(left, bottom - cornerSize, left + cornerSize, bottom),
                new Square(right - cornerSize, bottom - cornerSize, right, bottom));
    }

    public Optional<Square> shrunkBy(int margin) {
        Square shrunk = new Square(left + margin, top + margin, right - margin, bottom - margin);
        return shrunk.width() > 0 && shrunk.height() > 0 ? Optional.of(shrunk) : Optional.empty();
    }
}
