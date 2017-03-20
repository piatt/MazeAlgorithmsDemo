package com.slalom.bishop.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.annimon.stream.Stream;

public class MazeView extends View {
    private int rows, columns;
    private float cellHeight, cellWidth;
    private Paint closedWallPaint = new Paint();
    private Paint openWallPaint = new Paint();
    private Paint currentWallPaint = new Paint();
    private Paint cellPaint = new Paint();
    private Paint pathPaint = new Paint();
    private Maze maze;

    public MazeView(Context context) {
        this(context, null);
    }

    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configureView();
    }

    private void configureView() {
        closedWallPaint.setStyle(Paint.Style.STROKE);
        closedWallPaint.setColor(Color.BLACK);
        closedWallPaint.setStrokeWidth(10);
        openWallPaint.setStyle(Paint.Style.STROKE);
        openWallPaint.setColor(Color.TRANSPARENT);
        openWallPaint.setStrokeWidth(10);
        currentWallPaint.setStyle(Paint.Style.STROKE);
        currentWallPaint.setStrokeWidth(10);
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setColor(Color.WHITE);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(20);
        pathPaint.setColor(Color.BLUE);
    }

    public void setMaze(Maze maze) {
        this.maze = maze;
        this.rows = maze.getRows();
        this.columns = maze.getColumns();
        calculateDimensions();
    }

    private void calculateDimensions() {
        if (rows > 0 && columns > 0) {
            cellHeight = getHeight() / (float) rows;
            cellWidth = getWidth() / (float) columns;
        }
        invalidate();
    }

    private RectF getCellCoordinates(int row, int column) {
        return new RectF(column * cellWidth, row * cellHeight, (column + 1) * cellWidth, (row + 1) * cellHeight);
    }

    private float[] getWallCoordinates(int row, int column, MazeDirection direction) {
        switch (direction) {
            case TOP: return new float[] { column * cellWidth, row * cellHeight, (column + 1) * cellWidth, row * cellHeight };
            case BOTTOM: return new float[] { column * cellWidth, (row + 1) * cellHeight, (column + 1) * cellWidth, (row + 1) * cellHeight };
            case LEFT: return new float[] { column * cellWidth, row * cellHeight, column * cellWidth, (row + 1) * cellHeight };
            case RIGHT: return new float[] { (column + 1) * cellWidth, row * cellHeight, (column + 1) * cellWidth, (row + 1) * cellHeight };
            default: return new float[] {};
        }
    }

    private void drawWalls(Canvas canvas, MazeCell cell) {
        int row = cell.getRow();
        int column = cell.getColumn();

        canvas.drawLines(getWallCoordinates(row, column, MazeDirection.TOP), cell.hasTopWall() ? closedWallPaint : openWallPaint);
        canvas.drawLines(getWallCoordinates(row, column, MazeDirection.BOTTOM), cell.hasBottomWall() ? closedWallPaint : openWallPaint);
        canvas.drawLines(getWallCoordinates(row, column, MazeDirection.LEFT), cell.hasLeftWall() ? closedWallPaint : openWallPaint);
        canvas.drawLines(getWallCoordinates(row, column, MazeDirection.RIGHT), cell.hasRightWall() ? closedWallPaint : openWallPaint);

        MazeWall currentWall = cell.getCurrentWall();
        if (currentWall != null) {
            currentWallPaint.setColor(currentWall.isVisible() ? Color.RED : Color.GREEN);
            canvas.drawLines(getWallCoordinates(currentWall.getRow(), currentWall.getColumn(), currentWall.getDirection()),  currentWallPaint);
        }
    }

    private void drawCell(Canvas canvas, MazeCell cell, int color) {
        cellPaint.setColor(color);
        canvas.drawRect(getCellCoordinates(cell.getRow(), cell.getColumn()), cellPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (rows > 0 && columns > 0) {
            Stream.of(maze.getCells()).forEach(cell -> {
                if (!cell.isModifiable()) {
                    drawCell(canvas, cell, Color.WHITE);
                }
                if (cell.isExploring()) {
                    drawCell(canvas, cell, maze.getStepsToSolution() > 0 ? Color.YELLOW : Color.GREEN);
                }
                if (cell.isBacktracking()) {
                    drawCell(canvas, cell, Color.RED);
                }
                drawWalls(canvas, cell);
            });
        }
    }
}