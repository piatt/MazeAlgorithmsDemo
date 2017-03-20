package com.slalom.bishop.maze;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class MazeOptions implements Serializable {
    @Getter @Setter private int rows, columns, speed;
    @Getter @Setter private MazeAlgorithm algorithm;
    @Getter @Setter private MazeType type;

    enum MazeAlgorithm { BACKTRACK, KRUSKAL, PRIM }
    enum MazeType { ANIMATE, GENERATE, SOLVE }
}