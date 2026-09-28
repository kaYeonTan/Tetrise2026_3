package kr.ac.jbnu.se.tetris;

/**
 * Legacy entry point that delegates to the active application entry point.
 */
@Deprecated
public final class Tetris {
    private Tetris() {
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
