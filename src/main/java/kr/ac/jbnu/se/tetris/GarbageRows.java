package kr.ac.jbnu.se.tetris;

import java.util.function.IntSupplier;

final class GarbageRows {

    private static final int GARBAGE_BLOCK = 8;

    private GarbageRows() {
    }

    static boolean hasEmptyTopRow(int[][] board) {
        for (int cell : board[0]) {
            if (cell != 0) {
                return false;
            }
        }
        return true;
    }

    /** Inserts a row after the caller has checked that the board can rise. */
    static void insert(int[][] board, IntSupplier selectHole) {
        int width = board[0].length;
        int bottom = board.length - 1;
        System.arraycopy(board, 1, board, 0, bottom);

        int[] row = new int[width];
        board[bottom] = row;
        int hole = selectHole.getAsInt();
        for (int column = 0; column < width; column++) {
            row[column] = column == hole ? 0 : GARBAGE_BLOCK;
        }
    }
}
