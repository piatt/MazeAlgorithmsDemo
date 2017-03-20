package com.slalom.bishop.maze;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class MazeCell {
    @Getter private int row;
    @Getter private int column;
    @Setter private MazeCell rootCell;
    @Getter @Setter private boolean exploring;
    @Getter @Setter private boolean backtracking;
    @Getter @Setter private boolean modifiable = true;
    private Map<MazeDirection, MazeWall> directionToWallMap;

    public MazeCell(int row, int column) {
        this.row = row;
        this.column = column;
        directionToWallMap = new HashMap<>(4);
        directionToWallMap.put(MazeDirection.TOP, new MazeWall(row, column, MazeDirection.TOP));
        directionToWallMap.put(MazeDirection.BOTTOM, new MazeWall(row, column, MazeDirection.BOTTOM));
        directionToWallMap.put(MazeDirection.LEFT, new MazeWall(row, column, MazeDirection.LEFT));
        directionToWallMap.put(MazeDirection.RIGHT, new MazeWall(row, column, MazeDirection.RIGHT));
    }

    public void resetVisualIndicators() {
        exploring = false;
        backtracking = false;
    }

    private MazeCell getRootCell() {
        return rootCell != null ? rootCell.getRootCell() : this;
    }

    public boolean isConnected(MazeCell cell) {
        return getRootCell() == cell.getRootCell();
    }

    public void connect(MazeCell cell, MazeDirection direction) {
        setModifiable(false);
        cell.setModifiable(false);
        removeWall(direction);
        cell.removeWall(MazeDirection.getOppositeDirection(direction));
        cell.getRootCell().setRootCell(this);
    }

    public List<MazeWall> getWalls() {
        return Stream.of(directionToWallMap.values()).collect(Collectors.toList());
    }

    public MazeWall getWall(MazeDirection direction) {
        return directionToWallMap.get(direction);
    }

    public MazeWall getCurrentWall() {
        Optional<MazeWall> currentWallOptional = Stream.of(getWalls()).filter(MazeWall::isCurrentWall).findFirst();
        return currentWallOptional.isPresent() ? currentWallOptional.get() : null;
    }

    public void removeWall(MazeDirection direction) {
        getWall(direction).setVisible(false);
    }

    public boolean hasTopWall() {
        return getWall(MazeDirection.TOP).isVisible();
    }

    public boolean hasBottomWall() {
        return getWall(MazeDirection.BOTTOM).isVisible();
    }

    public boolean hasLeftWall() {
        return getWall(MazeDirection.LEFT).isVisible();
    }

    public boolean hasRightWall() {
        return getWall(MazeDirection.RIGHT).isVisible();
    }
}