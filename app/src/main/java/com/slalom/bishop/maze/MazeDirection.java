package com.slalom.bishop.maze;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum MazeDirection {
    TOP, BOTTOM, LEFT, RIGHT, CENTER;

    public static MazeDirection getOppositeDirection(MazeDirection direction) {
        switch (direction) {
            case TOP: return BOTTOM;
            case BOTTOM: return TOP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
            default: return CENTER;
        }
    }

    public static List<MazeDirection> getRandomizedDirections() {
        List<MazeDirection> directions = Stream.of(Arrays.asList(values()))
                .filter(direction -> !direction.equals(CENTER)).collect(Collectors.toList());
        Collections.shuffle(directions);
        return directions;
    }
}