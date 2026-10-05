package com.humanvision.checkbox.service.detector;

import java.util.stream.IntStream;

final class DisjointSets {
    private final int[] parentOf;

    DisjointSets(int elementCount) {
        this.parentOf = IntStream.range(0, elementCount).toArray();
    }

    int representativeOf(int element) {
        int current = element;
        while (parentOf[current] != current) {
            parentOf[current] = parentOf[parentOf[current]];
            current = parentOf[current];
        }
        return current;
    }

    void merge(int firstElement, int secondElement) {
        parentOf[representativeOf(firstElement)] = representativeOf(secondElement);
    }
}
