package com.slalom.bishop.maze;

import lombok.Getter;
import lombok.Setter;

public class MazeWall {
    @Getter private int row;
    @Getter private int column;
    @Getter private MazeDirection direction;
    @Getter @Setter private boolean currentWall;
    @Getter @Setter private boolean visible = true;

    public MazeWall(int row, int column, MazeDirection direction) {
        this.row = row;
        this.column = column;
        this.direction = direction;
    }
}