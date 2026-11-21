package kr.ac.jbnu.se.tetris;

import java.awt.Rectangle;

/** 환경설정 화면의 버튼 좌표. 그리기와 클릭 판정이 같은 값을 쓴다. */
public final class SettingsLayout {
    public static final int TAB_COUNT = 4;
    public static final Rectangle RESET = new Rectangle(250, 590, 170, 46);
    public static final Rectangle SAVE = new Rectangle(700, 590, 150, 46);
    public static final Rectangle CLOSE = new Rectangle(862, 590, 150, 46);

    private static final int ROW_TOP = 190;
    private static final int ROW_STEP = 48;

    private SettingsLayout() {
    }

    public static Rectangle tab(int index) {
        return new Rectangle(250 + index * 192, 126, 180, 42);
    }

    static int rowY(int row) {
        return ROW_TOP + row * ROW_STEP;
    }

    public static Rectangle keyButton(int row) {
        return new Rectangle(670, rowY(row), 220, 36);
    }
}
