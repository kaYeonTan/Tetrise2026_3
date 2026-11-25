package kr.ac.jbnu.se.tetris.boss;

import java.util.ArrayDeque;

public final class Blueprint {

    public static final int ROWS = 6;
    public static final int COLUMNS = 10;
    private static final String[][] STAGE_THREE_PATTERNS = {
            {
                    "E--TTT---E",
                    "-ETTTT---E",
                    "ETTTTT---E",
                    "TTTTTT-E--",
                    "TTTTTT--E-",
                    "TTTETTT-E-"
            },
            {
                    "E---TTT--E",
                    "--ETTTT-E-",
                    "-ETTTTT--E",
                    "E-TTTTTT--",
                    "-TTTTTT-E-",
                    "TTTETTT-E-"
            },
            {
                    "E----TTT-E",
                    "E---TTTT-E",
                    "E--TTTTT-E",
                    "E-TTTTTT--",
                    "ETTTTTT---",
                    "E-TTTETTT-"
            },
            {
                    "E--TTTT--E",
                    "-E-TTTT-E-",
                    "E-TTTTT--E",
                    "-TTTTTT-E-",
                    "ETTTTTT---",
                    "TTETTTT-E-"
            },
            {
                    "E-TTTT---E",
                    "E-TTTTT-E-",
                    "-ETTTTTT-E",
                    "E-TTTTTT--",
                    "-TTTTTT-E-",
                    "TTTTETTT-E"
            },
            {
                    "E-TTT----E",
                    "-ETTTT---E",
                    "E-TTTTT-E-",
                    "-TTTTTT-E-",
                    "E-TTTTTT--",
                    "TTTETTT-E-"
            }
    };

    @FunctionalInterface
    public interface Occupancy {
        boolean occupied(int row, int column);
    }

    private final boolean[][] target;
    private final boolean[][] mustBeEmpty;
    private final int targetCount;

    private Blueprint(boolean[][] target, boolean[][] mustBeEmpty) {
        this.target = target;
        this.mustBeEmpty = mustBeEmpty;
        int count = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (target[row][column]) {
                    count++;
                }
            }
        }
        targetCount = count;
    }

    public static Blueprint stageThree() {
        return stageThree(0);
    }

    static Blueprint stageThree(int index) {
        if (index < 0 || index >= STAGE_THREE_PATTERNS.length) {
            throw new IllegalArgumentException("Unknown stage three blueprint: " + index);
        }
        return fromRows(STAGE_THREE_PATTERNS[index]);
    }

    static int stageThreePatternCount() {
        return STAGE_THREE_PATTERNS.length;
    }

    static Blueprint fromRows(String... rows) {
        if (rows.length != ROWS) {
            throw new IllegalArgumentException("Blueprint must have exactly " + ROWS + " rows");
        }
        boolean[][] target = new boolean[ROWS][COLUMNS];
        boolean[][] empty = new boolean[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            if (rows[row].length() != COLUMNS) {
                throw new IllegalArgumentException("Each blueprint row must have exactly " + COLUMNS + " columns");
            }
            boolean hasEmptyCell = false;
            for (int column = 0; column < COLUMNS; column++) {
                char cell = rows[row].charAt(column);
                if (cell == 'T') {
                    target[row][column] = true;
                } else if (cell == 'E') {
                    empty[row][column] = true;
                    hasEmptyCell = true;
                } else if (cell != '-') {
                    throw new IllegalArgumentException("Unknown blueprint cell: " + cell);
                } else {}
            }
            if (!hasEmptyCell) {
                throw new IllegalArgumentException("Every blueprint row needs a must-be-empty cell");
            }
        }
        validateTargetShape(target);
        return new Blueprint(target, empty);
    }

    private static void validateTargetShape(boolean[][] target) {
        int startColumn = requireBottomTargetColumn(target);
        int connectedCount = countConnectedTargets(target, startColumn);
        int targetCount = countTargetCells(target);

        if (connectedCount != targetCount) {
            throw new IllegalArgumentException("All blueprint target cells must be connected");
        }
    }

    private static int requireBottomTargetColumn(boolean[][] target) {
        for (int column = 0; column < COLUMNS; column++) {
            if (target[ROWS - 1][column]) {
                return column;
            }
        }
        throw new IllegalArgumentException("Blueprint bottom row needs at least one target cell");
    }

    private static int countConnectedTargets(boolean[][] target, int startColumn) {
        boolean[][] visited = new boolean[ROWS][COLUMNS];
        ArrayDeque<int[]> pending = new ArrayDeque<>();
        pending.add(new int[]{ROWS - 1, startColumn});
        visited[ROWS - 1][startColumn] = true;
        int connectedCount = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!pending.isEmpty()) {
            int[] cell = pending.removeFirst();
            connectedCount++;
            for (int[] direction : directions) {
                int row = cell[0] + direction[0];
                int column = cell[1] + direction[1];
                if (isInBounds(row, column) && target[row][column] && !visited[row][column]) {
                    visited[row][column] = true;
                    pending.addLast(new int[]{row, column});
                }
            }
        }
        return connectedCount;
    }

    private static boolean isInBounds(int row, int column) {
        return row >= 0 && row < ROWS && column >= 0 && column < COLUMNS;
    }

    private static int countTargetCells(boolean[][] target) {
        int targetCount = 0;
        for (boolean[] row : target) {
            for (boolean cell : row) {
                if (cell) {
                    targetCount++;
                }
            }
        }
        return targetCount;
    }

    public boolean isTarget(int row, int column) {
        return target[row][column];
    }

    public boolean mustBeEmpty(int row, int column) {
        return mustBeEmpty[row][column];
    }

    public double fillRatio(Occupancy occupancy) {
        int filled = countOccupiedCells(target, occupancy);
        return targetCount == 0 ? 0.0 : (double) filled / targetCount;
    }

    public int violationCount(Occupancy occupancy) {
        return countOccupiedCells(mustBeEmpty, occupancy);
    }

    public boolean isSatisfied(Occupancy occupancy) {
        return fillRatio(occupancy) >= 1.0 && violationCount(occupancy) == 0;
    }

    private static int countOccupiedCells(boolean[][] mask, Occupancy occupancy) {
        int count = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (mask[row][column] && occupancy.occupied(row, column)) {
                    count++;
                }
            }
        }
        return count;
    }
}
