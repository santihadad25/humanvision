package com.humanvision.checkbox.model.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

public enum FileType {
    PNG(List.of(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)),
    JPEG(List.of(0xFF, 0xD8, 0xFF)),
    PDF(List.of(0x25, 0x50, 0x44, 0x46, 0x2D));

    private final List<Integer> signature;

    FileType(List<Integer> signature) {
        this.signature = List.copyOf(signature);
    }

    public boolean matches(byte[] content) {
        return content != null
                && content.length >= signature.size()
                && IntStream.range(0, signature.size())
                        .allMatch(index -> (content[index] & 0xFF) == signature.get(index));
    }

    public static Optional<FileType> detect(byte[] content) {
        return Arrays.stream(values()).filter(type -> type.matches(content)).findFirst();
    }

    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }
}
