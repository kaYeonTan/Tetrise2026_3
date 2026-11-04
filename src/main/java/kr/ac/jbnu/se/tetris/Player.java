package kr.ac.jbnu.se.tetris;

import java.util.ArrayList;

/** One player's board, falling piece and counters; owns the movement rules that used to be duplicated per player. */
public final class Player {

    public static final int ROWS = 24;
    public static final int COLUMNS = 10;
    private static final int SPAWN_X = 3;
    private static final int SPAWN_Y = 3;
    private static final int[] LINE_SCORE = {0, 100, 300, 500, 800};

    public int[][] board = new int[ROWS][COLUMNS];
    public int type;
    public int rot;
    public int x;
    public int y;
    public int hold = -1;
    public boolean holdUsed;
    public final ArrayList<Integer> next = new ArrayList<>();
    public final ArrayList<Integer> bag = new ArrayList<>();
    public int score;
    public int lines;
    public int garbage;
    public int dropCount;
    public int leftCount;
    public int rightCount;
    public int downCount;
    public boolean fastNext;
    public boolean fastCurrent;

    public static boolean canPlace(int[][] board, int type, int rot, int x, int y) {
        if (type < 0 || type >= GamePanel.SHAPES.length || rot < 0 || rot >= GamePanel.SHAPES[type].length) {
            return false;
        }
        for (int i = 0; i < GamePanel.blockCount(type); i++) {
            int bx = x + GamePanel.SHAPES[type][rot][i * 2];
            int by = y + GamePanel.SHAPES[type][rot][i * 2 + 1];
            if (bx < 0 || bx >= COLUMNS || by < 0 || by >= ROWS) {
                return false;
            }
            if (board[by][bx] != 0) {
                return false;
            }
        }
        return true;
    }

    /** Resets everything that belongs to one stage attempt (the board and falling-piece counters). */
    public void resetForStage() {
        board = new int[ROWS][COLUMNS];
        garbage = 0;
        holdUsed = false;
        dropCount = 0;
    }

    /** Resets the run-level state used by a new game. */
    public void resetForNewGame() {
        score = 0;
        lines = 0;
        hold = -1;
        next.clear();
        bag.clear();
    }

    public void resetInputRepeat() {
        leftCount = 0;
        rightCount = 0;
        downCount = 0;
    }

    /** Advances the held-left timer by one frame; true when a repeat move is due. */
    public boolean repeatLeft(int frameMillis) {
        leftCount += frameMillis;
        if (leftCount < GamePanel.HORIZONTAL_DAS_DELAY_MS) {
            return false;
        }
        leftCount -= GamePanel.HORIZONTAL_ARR_INTERVAL_MS;
        return true;
    }

    public boolean repeatRight(int frameMillis) {
        rightCount += frameMillis;
        if (rightCount < GamePanel.HORIZONTAL_DAS_DELAY_MS) {
            return false;
        }
        rightCount -= GamePanel.HORIZONTAL_ARR_INTERVAL_MS;
        return true;
    }

    public boolean repeatDown(int frameMillis) {
        downCount += frameMillis;
        if (downCount < GamePanel.SOFT_DROP_REPEAT_MS) {
            return false;
        }
        downCount = 0;
        return true;
    }

    /** Puts the already chosen {@link #type} at the spawn position. */
    public void startPiece() {
        rot = 0;
        x = SPAWN_X;
        y = SPAWN_Y;
        fastCurrent = fastNext;
        fastNext = false;
        dropCount = 0;
    }

    /** Deep copy of this player's state (memento); the save format is untouched. */
    public Player copy() {
        Player c = new Player();
        c.copyFrom(this);
        return c;
    }

    public void copyFrom(Player o) {
        board = new int[ROWS][];
        for (int r = 0; r < ROWS; r++) {
            board[r] = o.board[r].clone();
        }
        type = o.type;
        rot = o.rot;
        x = o.x;
        y = o.y;
        hold = o.hold;
        holdUsed = o.holdUsed;
        next.clear();
        next.addAll(o.next);
        bag.clear();
        bag.addAll(o.bag);
        score = o.score;
        lines = o.lines;
        garbage = o.garbage;
        dropCount = o.dropCount;
        leftCount = o.leftCount;
        rightCount = o.rightCount;
        downCount = o.downCount;
        fastNext = o.fastNext;
        fastCurrent = o.fastCurrent;
    }

    public boolean fits() {
        return canPlace(board, type, rot, x, y);
    }

    public boolean move(int dx) {
        if (!canPlace(board, type, rot, x + dx, y)) {
            return false;
        }
        x += dx;
        return true;
    }

    public boolean softDrop() {
        if (!canPlace(board, type, rot, x, y + 1)) {
            return false;
        }
        y++;
        score += 1;
        dropCount = 0;
        return true;
    }

    /** One gravity step without score; false when the piece is resting. */
    public boolean fall() {
        if (!canPlace(board, type, rot, x, y + 1)) {
            return false;
        }
        y++;
        return true;
    }

    public void dropToFloor() {
        while (canPlace(board, type, rot, x, y + 1)) {
            y++;
            score += 2;
        }
    }

    public boolean rotate(int dir) {
        if (type == 1) {
            return false;
        }
        int newRot = (rot + dir + 4) % 4;
        int[] kick = GamePanel.KICK[type == 0 ? 1 : 0][dir == 1 ? rot : 4 + rot];
        for (int i = 0; i < 5; i++) {
            int nx = x + kick[i * 2];
            int ny = y - kick[i * 2 + 1];
            if (canPlace(board, type, newRot, nx, ny)) {
                x = nx;
                y = ny;
                rot = newRot;
                return true;
            }
        }
        return false;
    }

    /**
     * Stores the current piece in the hold slot. Returns true when the slot was empty, so the caller has to
     * spawn the next piece; otherwise the held piece is already swapped in at the spawn position.
     */
    public boolean stashCurrent() {
        if (hold == -1) {
            hold = type;
            return true;
        }
        int swapped = hold;
        hold = type;
        type = swapped;
        rot = 0;
        x = SPAWN_X;
        y = SPAWN_Y;
        return false;
    }

    public void placeOnBoard() {
        for (int i = 0; i < GamePanel.blockCount(type); i++) {
            board[y + GamePanel.SHAPES[type][rot][i * 2 + 1]][x + GamePanel.SHAPES[type][rot][i * 2]] =
                    GamePanel.blockValue(type);
        }
    }

    public void addLineScore(int cleared, int stage) {
        lines += cleared;
        if (cleared >= 1 && cleared <= 4) {
            score += LINE_SCORE[cleared] * stage;
        }
    }

    /** Lowest row occupied by the falling piece must stay on the board after the stack is raised by one row. */
    public boolean canRaiseStack() {
        for (int c = 0; c < COLUMNS; c++) {
            if (board[0][c] != 0) {
                return false;
            }
        }
        for (int i = 0; i < GamePanel.blockCount(type); i++) {
            if (y + GamePanel.SHAPES[type][rot][i * 2 + 1] - 1 < 0) {
                return false;
            }
        }
        return true;
    }
}
