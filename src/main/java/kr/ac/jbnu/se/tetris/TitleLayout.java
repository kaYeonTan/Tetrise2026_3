package kr.ac.jbnu.se.tetris;

import java.awt.Rectangle;

/** 타이틀 버튼 좌표의 단일 출처. 그리기와 클릭 판정이 같은 값을 쓰도록 한다. */
public final class TitleLayout {
    public static final Rectangle SINGLE = new Rectangle(88, 262, 300, 54);
    public static final Rectangle MULTI = new Rectangle(88, 332, 300, 54);
    public static final Rectangle RANKING = new Rectangle(88, 402, 300, 54);
    public static final Rectangle SETTINGS = new Rectangle(88, 472, 300, 54);
    public static final Rectangle QUIT = new Rectangle(88, 542, 300, 44);
    public static final Rectangle LOGIN = new Rectangle(88, 600, 150, 44);
    public static final Rectangle SIGN_UP = new Rectangle(250, 600, 150, 44);

    private TitleLayout() {
    }
}
