package com.humanvision.checkbox.service.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class DisjointSetsTest {

    @Test
    void everyElementStartsAloneInItsOwnSet() {
        DisjointSets sets = new DisjointSets(3);

        assertNotEquals(sets.representativeOf(0), sets.representativeOf(1));
    }

    @Test
    void mergedElementsShareARepresentativeEvenThroughAChain() {
        DisjointSets sets = new DisjointSets(5);

        sets.merge(0, 1);
        sets.merge(1, 2);
        sets.merge(3, 4);

        assertEquals(sets.representativeOf(0), sets.representativeOf(2));
        assertEquals(sets.representativeOf(3), sets.representativeOf(4));
        assertNotEquals(sets.representativeOf(0), sets.representativeOf(3));
    }
}
