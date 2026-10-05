package com.humanvision.checkbox.service.detector;

import com.humanvision.checkbox.model.detector.Square;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class DominantSizeFilter {
    private static final double LARGEST_SIZE_RATIO_WITHIN_A_GROUP = 1.2;
    private static final double MIN_SHARE_OF_LARGEST_GROUP_TO_KEEP = 0.2;
    private static final int GROUP_SIZE_NEEDED_TO_JUDGE_OUTLIERS = 4;
    private static final int MIN_GROUP_SIZE_WHEN_JUDGING = 2;

    List<Square> keepDominantSizes(List<Square> squares) {
        List<List<Square>> sizeGroups = groupBySimilarSize(squares);
        int smallestGroupToKeep = smallestGroupSizeToKeep(largestGroupSize(sizeGroups));
        return sizeGroups.stream()
                .filter(group -> group.size() >= smallestGroupToKeep)
                .flatMap(List::stream)
                .toList();
    }

    private static List<List<Square>> groupBySimilarSize(List<Square> squares) {
        List<List<Square>> sizeGroups = new ArrayList<>();
        for (Square square : smallestFirst(squares)) {
            if (isTooLargeForLastGroup(sizeGroups, square)) {
                sizeGroups.add(new ArrayList<>());
            }
            sizeGroups.get(sizeGroups.size() - 1).add(square);
        }
        return sizeGroups;
    }

    private static List<Square> smallestFirst(List<Square> squares) {
        return squares.stream().sorted(Comparator.comparingInt(Square::averageSide)).toList();
    }

    private static boolean isTooLargeForLastGroup(List<List<Square>> sizeGroups, Square square) {
        if (sizeGroups.isEmpty()) {
            return true;
        }
        List<Square> lastGroup = sizeGroups.get(sizeGroups.size() - 1);
        int smallestSideInGroup = lastGroup.get(0).averageSide();
        return square.averageSide() > smallestSideInGroup * LARGEST_SIZE_RATIO_WITHIN_A_GROUP;
    }

    private static int largestGroupSize(List<List<Square>> sizeGroups) {
        return sizeGroups.stream().mapToInt(List::size).max().orElse(0);
    }

    private static int smallestGroupSizeToKeep(int largestGroupSize) {
        if (largestGroupSize < GROUP_SIZE_NEEDED_TO_JUDGE_OUTLIERS) {
            return 1;
        }
        int shareOfLargestGroup = (int) Math.ceil(largestGroupSize * MIN_SHARE_OF_LARGEST_GROUP_TO_KEEP);
        return Math.max(MIN_GROUP_SIZE_WHEN_JUDGING, shareOfLargestGroup);
    }
}
