package com.slalom.bishop.maze;

import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Maze {
    @Getter private int rows, columns;
    @Getter private int stepsToSolution;
    @Getter private MazeOptions options;
    private int speed = 100;
    private Random random = new Random();
    private MazeWall currentWall;
    private MazeCell currentCell;
    private MazeCell[][] cells;

    public Maze(MazeOptions options) {
        this.options = options;
        if (options.getRows() > 0 && options.getColumns() > 0) {
            rows = options.getRows();
            columns = options.getColumns();
            speed = options.getSpeed();
            initCells();
        } else {
            Log.e(getClass().getSimpleName(), "Maze dimensions must be greater than zero!");
        }
    }

    private void initCells() {
        cells = IntStream.range(0, rows)
                .mapToObj(row -> IntStream.range(0, columns)
                        .mapToObj(column -> new MazeCell(row, column))
                        .toArray(MazeCell[]::new))
                .toArray(MazeCell[][]::new);
    }

    /**
     * RECURSIVE BACKTRACK
     */

    public Observable<Object> animateViaRecursiveBacktrack() {
        Stack<MazeCell> cellStack = new Stack<>();
        List<MazeCell> modifiableCells = getCells();
        currentCell = getRandomCell(modifiableCells);
        currentCell.setModifiable(false);
        modifiableCells.remove(currentCell);

        return Observable.create(subscriber -> {
            MazeCell modifiableAdjacentCell = getRandomModifiableAdjacentCell(currentCell);

            if (modifiableAdjacentCell != null) {
                cellStack.push(currentCell);
                removeAdjacentWalls(currentCell, modifiableAdjacentCell);
                currentCell.resetVisualIndicators();
                currentCell = modifiableAdjacentCell;
                currentCell.setModifiable(false);
                currentCell.setExploring(true);
                modifiableCells.remove(currentCell);
            } else if (!cellStack.isEmpty()) {
                currentCell.resetVisualIndicators();
                currentCell = cellStack.pop();
                currentCell.setBacktracking(true);
            } else {
                currentCell.resetVisualIndicators();
                currentCell = getRandomCell(modifiableCells);
                currentCell.setModifiable(false);
                currentCell.setExploring(true);
                modifiableCells.remove(currentCell);
            }

            subscriber.onNext(modifiableCells.size());
            subscriber.onCompleted();
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .repeatWhen(completed -> completed.delay(speed, TimeUnit.MILLISECONDS))
        .takeUntil(result -> modifiableCells.isEmpty())
        .doOnTerminate(() -> currentCell.resetVisualIndicators());
    }

    public void generateViaRecursiveBacktrack() {
        MazeCell cell = getRandomCell(getCells());
        runRecursiveBacktrack(cell);
    }

    private void runRecursiveBacktrack(MazeCell cell) {
        cell.setModifiable(false);
        List<MazeDirection> randomizedDirections = MazeDirection.getRandomizedDirections();

        Stream.of(randomizedDirections).forEach(direction -> {
            MazeCell adjacentCell = getAdjacentCell(cell.getRow(), cell.getColumn(), direction);
            if (adjacentCell != null && adjacentCell.isModifiable()) {
                removeAdjacentWalls(cell, adjacentCell);
                runRecursiveBacktrack(adjacentCell);
            }
        });
    }

    public Observable<Object> solveViaRecursiveBacktrack() {
        return Observable.create(subscriber -> {
            stepsToSolution = 0;
            MazeCell cell = getCell(0, 0);
            if (hasPath(cell)) {
                stepsToSolution++;
                cell.setExploring(true);
            }

            subscriber.onNext(stepsToSolution);
            subscriber.onCompleted();
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread());
    }

    private boolean hasPath(MazeCell mazeCell) {
        if (mazeCell.getRow() == rows - 1 && mazeCell.getColumn() == columns - 1) {
            stepsToSolution++;
            mazeCell.setExploring(true);
            return true;
        }

        List<MazeCell> explorableAdjacentMazeCells = getExplorableAdjacentMazeCells(mazeCell);
        for (MazeCell explorableMazeCell : explorableAdjacentMazeCells) {
            explorableMazeCell.setExploring(true);
            if (hasPath(explorableMazeCell)) {
                stepsToSolution++;
                return true;
            }
            explorableMazeCell.setExploring(false);
        }

        return false;
    }

    /**
     * KRUSKAL'S ALGORITHM
     */

    public Observable<Object> animateViaKruskalsAlgorithm() {
        List<MazeWall> walls = getRandomizedInnerWalls();

        return Observable.create(subscriber -> {
            if (currentWall != null) {
                currentWall.setCurrentWall(false);
            }
            currentWall = walls.remove(0);
            currentWall.setCurrentWall(true);

            runKruskalsAlgorithm(currentWall, walls);

            subscriber.onNext(walls.size());
            subscriber.onCompleted();
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .repeatWhen(completed -> completed.delay(speed, TimeUnit.MILLISECONDS))
        .takeUntil(result -> walls.isEmpty())
        .doOnTerminate(() -> currentWall.setCurrentWall(false));
    }

    public void generateViaKruskalsAlgorithm() {
        List<MazeWall> walls = getRandomizedInnerWalls();
        while (!walls.isEmpty()) {
            MazeWall wall = walls.remove(0);
            runKruskalsAlgorithm(wall, walls);
        }
    }

    private void runKruskalsAlgorithm(MazeWall wall, List<MazeWall> walls) {
        MazeCell cell = getCell(wall.getRow(), wall.getColumn());
        MazeCell adjacentCell = getAdjacentCell(wall.getRow(), wall.getColumn(), wall.getDirection());

        if (adjacentCell != null && !cell.isConnected(adjacentCell)) {
            cell.connect(adjacentCell, wall.getDirection());
            MazeDirection oppositeDirection = MazeDirection.getOppositeDirection(wall.getDirection());
            MazeWall adjacentWall = adjacentCell.getWall(oppositeDirection);
            walls.remove(adjacentWall);
        }
    }

    private List<MazeWall> getRandomizedInnerWalls() {
        List<MazeWall> walls = Stream.of(getCells()).flatMap(cell -> Stream.of(cell.getWalls()))
                .filter(wall -> getAdjacentCell(wall.getRow(), wall.getColumn(), wall.getDirection()) != null).collect(Collectors.toList());
        Collections.shuffle(walls);
        return walls;
    }

    /**
     * PRIM'S ALGORITHM
     */

    public Observable<Object> animateViaPrimsAlgorithm() {
        Set<MazeCell> frontierCells = new HashSet<>();
        MazeCell cell = getRandomCell(getCells());
        cell.setModifiable(false);
        frontierCells.addAll(getAdjacentMazeCells(cell));

        return Observable.create(subscriber -> {
            if (currentCell != null) {
                currentCell.setExploring(false);
            }
            currentCell = getRandomFrontierCell(frontierCells);
            currentCell.setExploring(true);

            runPrimsAlgorithm(currentCell, frontierCells);

            subscriber.onNext(frontierCells.size());
            subscriber.onCompleted();
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .repeatWhen(completed -> completed.delay(speed, TimeUnit.MILLISECONDS))
        .takeUntil(result -> frontierCells.isEmpty())
        .doOnTerminate(() -> currentCell.setExploring(false));
    }

    public void generateViaPrimsAlgorithm() {
        Set<MazeCell> frontierCells = new HashSet<>();
        MazeCell cell = getRandomCell(getCells());
        cell.setModifiable(false);
        frontierCells.addAll(getAdjacentMazeCells(cell));

        while (!frontierCells.isEmpty()) {
            MazeCell frontierCell = getRandomFrontierCell(frontierCells);
            runPrimsAlgorithm(frontierCell, frontierCells);
        }
    }

    private void runPrimsAlgorithm(MazeCell cell, Set<MazeCell> cells) {
        if (cell != null) {
            MazeCell adjacentSolutionCell = getRandomUnmodifiableAdjacentCell(cell);
            if (adjacentSolutionCell != null) {
                cells.addAll(getFrontierCells(cell));
                removeAdjacentWalls(cell, adjacentSolutionCell);
                cell.setModifiable(false);
                cell.setBacktracking(false);
                cells.remove(cell);
            }
        }
    }

    private MazeCell getRandomFrontierCell(Set<MazeCell> cells) {
        List<MazeCell> frontierCells = Stream.of(cells).collect(Collectors.toList());
        return !frontierCells.isEmpty() ? frontierCells.get(random.nextInt(frontierCells.size())) : null;
    }

    private MazeCell getRandomUnmodifiableAdjacentCell(MazeCell mazeCell) {
        List<MazeCell> unmodifiableAdjacentMazeCells = Stream.of(getAdjacentMazeCells(mazeCell)).filterNot(MazeCell::isModifiable).collect(Collectors.toList());
        return !unmodifiableAdjacentMazeCells.isEmpty() ? unmodifiableAdjacentMazeCells.get(random.nextInt(unmodifiableAdjacentMazeCells.size())) : null;
    }

    private List<MazeCell> getFrontierCells(MazeCell cell) {
        List<MazeCell> frontierCells = getModifiableAdjacentMazeCells(cell);
        Stream.of(frontierCells).forEach(frontierCell -> frontierCell.setBacktracking(true));
        return frontierCells;
    }

    /**
     * HELPER METHODS
     */

    public List<MazeCell> getCells() {
        return Stream.of(cells).flatMap(Stream::of).collect(Collectors.toList());
    }

    private MazeCell getCell(int row, int column) {
        boolean isCellInMaze = row >= 0 && column >= 0 && row < rows && column < columns;
        return isCellInMaze ? cells[row][column] : null;
    }

    public MazeCell getRandomCell(List<MazeCell> cells) {
        if (!cells.isEmpty()) {
            return cells.get(random.nextInt(cells.size()));
        }
        return null;
    }

    private MazeCell getAdjacentCell(int row, int column, MazeDirection mazeDirection) {
        switch (mazeDirection) {
            case TOP: return getCell(row - 1, column);
            case BOTTOM: return getCell(row + 1, column);
            case LEFT: return getCell(row, column - 1);
            case RIGHT: return getCell(row, column + 1);
        }
        return null;
    }

    private MazeCell getRandomModifiableAdjacentCell(MazeCell mazeCell) {
        List<MazeCell> modifiableAdjacentMazeCells = getModifiableAdjacentMazeCells(mazeCell);
        return !modifiableAdjacentMazeCells.isEmpty() ? modifiableAdjacentMazeCells.get(random.nextInt(modifiableAdjacentMazeCells.size())) : null;
    }

    private List<MazeCell> getAdjacentMazeCells(MazeCell mazeCell) {
        int row = mazeCell.getRow();
        int column = mazeCell.getColumn();

        List<MazeCell> adjacentMazeCells = Arrays.asList(
                getAdjacentCell(row, column, MazeDirection.TOP),
                getAdjacentCell(row, column, MazeDirection.BOTTOM),
                getAdjacentCell(row, column, MazeDirection.LEFT),
                getAdjacentCell(row, column, MazeDirection.RIGHT));

        return Stream.of(adjacentMazeCells).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<MazeCell> getModifiableAdjacentMazeCells(MazeCell mazeCell) {
        return Stream.of(getAdjacentMazeCells(mazeCell)).filter(MazeCell::isModifiable).collect(Collectors.toList());
    }

    private List<MazeCell> getExplorableAdjacentMazeCells(MazeCell mazeCell) {
        return Stream.of(getAdjacentMazeCells(mazeCell))
                .filterNot(MazeCell::isExploring).filter(adjacentMazeCell -> hasOpenAdjacentWall(mazeCell, adjacentMazeCell)).collect(Collectors.toList());
    }

    private MazeDirection getAdjacentWallDirection(MazeCell mazeCell, MazeCell adjacentMazeCell) {
        int row = mazeCell.getRow();
        int column = mazeCell.getColumn();
        int adjacentRow = adjacentMazeCell.getRow();
        int adjacentColumn = adjacentMazeCell.getColumn();

        if (row == adjacentRow + 1 && column == adjacentColumn) {
            return MazeDirection.TOP;
        } else if (row == adjacentRow - 1 && column == adjacentColumn) {
            return MazeDirection.BOTTOM;
        } else if (row == adjacentRow && column == adjacentColumn + 1) {
            return MazeDirection.LEFT;
        } else if (row == adjacentRow && column == adjacentColumn - 1) {
            return MazeDirection.RIGHT;
        } else {
            return MazeDirection.CENTER;
        }
    }

    private boolean hasOpenAdjacentWall(MazeCell cell, MazeCell adjacentCell) {
        MazeDirection adjacentDirection = getAdjacentWallDirection(cell, adjacentCell);
        switch (adjacentDirection) {
            case TOP: return !cell.hasTopWall() && !adjacentCell.hasBottomWall();
            case BOTTOM: return !cell.hasBottomWall() && !adjacentCell.hasTopWall();
            case LEFT: return !cell.hasLeftWall() && !adjacentCell.hasRightWall();
            case RIGHT: return !cell.hasRightWall() && !adjacentCell.hasLeftWall();
            default: return false;
        }
    }

    private void removeAdjacentWalls(MazeCell cell, MazeCell adjacentCell) {
        MazeDirection adjacentDirection = getAdjacentWallDirection(cell, adjacentCell);
        cell.removeWall(adjacentDirection);
        MazeDirection oppositeAdjacentDirection = MazeDirection.getOppositeDirection(adjacentDirection);
        adjacentCell.removeWall(oppositeAdjacentDirection);
    }
}