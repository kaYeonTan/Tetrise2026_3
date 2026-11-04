package kr.ac.jbnu.se.tetris;

import java.awt.Rectangle;

/** 저장 목록·랭킹 화면의 버튼 좌표. 그리기와 클릭 판정이 같은 값을 쓴다. */
final class UiLayout {
    static final Rectangle BACK = new Rectangle(40, 640, 140, 44);
    static final Rectangle NEW_GAME = new Rectangle(490, 612, 300, 54);
    static final Rectangle GO_LOGIN = new Rectangle(530, 420, 220, 46);
    static final Rectangle RANK_SCORE_TAB = new Rectangle(170, 146, 220, 46);
    static final Rectangle RANK_STAGE_TAB = new Rectangle(402, 146, 220, 46);

    static final int MAX_SAVE_ROWS = 5;
    private static final int SAVE_ROW_TOP = 146;
    private static final int SAVE_ROW_STEP = 88;

    private UiLayout() {
    }

    static int saveRowY(int index) {
        return SAVE_ROW_TOP + index * SAVE_ROW_STEP;
    }

    static Rectangle resumeButton(int index) {
        return new Rectangle(760, saveRowY(index) + 18, 120, 44);
    }

    static Rectangle deleteButton(int index) {
        return new Rectangle(890, saveRowY(index) + 18, 110, 44);
    }
}
