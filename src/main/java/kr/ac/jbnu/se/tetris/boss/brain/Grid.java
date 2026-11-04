package kr.ac.jbnu.se.tetris.boss.brain;

import java.util.Arrays;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.GameSession;

/** Immutable board snapshot used by the boss search. Row zero is the floor. */
public final class Grid {

    public static final int WIDTH = GameSession.WIDTH;
    public static final int HEIGHT = GameSession.HEIGHT;

    private final boolean[][] cells;

    private Grid(boolean[][] cells) {
        this.cells = cells;
    }

    public static Grid empty() {
        return new Grid(new boolean[HEIGHT][WIDTH]);
    }

    public static Grid from(GameSession session) {
        boolean[][] cells = new boolean[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                cells[y][x] = session.shapeAt(x, y) != Tetrominoes.NoShape;
            }
        }
        return new Grid(cells);
    }

    public static Grid fromOccupied(boolean[][] occupied) {
        if (occupied == null || occupied.length != HEIGHT) {
            throw new IllegalArgumentException("Expected " + HEIGHT + " board rows");
        }
        boolean[][] copy = new boolean[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            if (occupied[y] == null || occupied[y].length != WIDTH) {
                throw new IllegalArgumentException("Expected " + WIDTH + " board columns");
            }
            copy[y] = Arrays.copyOf(occupied[y], WIDTH);
        }
        return new Grid(copy);
    }

    public boolean occupied(int x, int y) {
        return cells[y][x];
    }

    public Grid raiseGarbage(int hole) {
        if (hole < 0 || hole >= WIDTH) {
            throw new IllegalArgumentException("Garbage hole must be on the board");
        }
        for (int x = 0; x < WIDTH; x++) {
            if (cells[HEIGHT - 1][x]) {
                return null;
            }
        }
        boolean[][] raised = new boolean[HEIGHT][WIDTH];
        for (int y = HEIGHT - 1; y > 0; y--) {
            raised[y] = Arrays.copyOf(cells[y - 1], WIDTH);
        }
        for (int x = 0; x < WIDTH; x++) {
            raised[0][x] = x != hole;
        }
        return new Grid(raised);
    }

    Grid placeAndClear(int[][] blocks, int originX, int originY) {
        boolean[][] placed = copyCells();
        for (int[] block : blocks) {
            placed[originY + block[1]][originX + block[0]] = true;
        }
        boolean[][] compacted = new boolean[HEIGHT][WIDTH];
        int targetY = 0;
        for (int y = 0; y < HEIGHT; y++) {
            boolean full = true;
            for (int x = 0; x < WIDTH; x++) {
                if (!placed[y][x]) {
                    full = false;
                    break;
                }
            }
            if (!full) {
                compacted[targetY++] = Arrays.copyOf(placed[y], WIDTH);
            }
        }
        return new Grid(compacted);
    }

    boolean canPlace(int[][] blocks, int originX, int originY) {
        for (int[] block : blocks) {
            int x = originX + block[0];
            int y = originY + block[1];
            if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || cells[y][x]) {
                return false;
            }
        }
        return true;
    }

    private boolean[][] copyCells() {
        boolean[][] copy = new boolean[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            copy[y] = Arrays.copyOf(cells[y], WIDTH);
        }
        return copy;
    }
}
