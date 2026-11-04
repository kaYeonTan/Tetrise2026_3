package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/** GamePanel의 화면 하나(State 패턴). 화면 번호별 분기를 객체로 대체한다. */
public interface PanelScreen {
    void render(Graphics2D g, GamePanel panel);

    /** 클릭을 처리했으면 true. */
    default boolean handleClick(GamePanel panel) {
        return false;
    }

    /** 키 입력을 처리했으면 true. */
    default boolean handleKey(GamePanel panel, KeyEvent e) {
        return false;
    }

    /** 타이머 틱마다 호출. */
    default void update(GamePanel panel) {
    }
}
