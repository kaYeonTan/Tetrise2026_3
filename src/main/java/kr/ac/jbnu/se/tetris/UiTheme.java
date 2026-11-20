package kr.ac.jbnu.se.tetris;

import java.awt.Color;

/** 타이틀·저장·랭킹 화면이 공유하는 색상 팔레트. 값을 바꾸면 화면 전체 분위기가 함께 바뀐다. */
final class UiTheme {
    static final Color TEXT_LIGHT = new Color(0xf0e2c8);
    static final Color TEXT_GOLD = new Color(0xd9c29a);
    static final Color HIGHLIGHT = new Color(0xe9c98f);
    static final Color TITLE_SHADOW = new Color(0x6b2a1a);
    static final Color INK_SHADOW = new Color(0x0c090d);
    static final Color INK = new Color(0x1a1216);
    static final Color BRASS = new Color(0xb89c6c);
    static final Color PARCHMENT = new Color(0xefe0bf);
    static final Color DANGER = new Color(0xa8352a);

    private UiTheme() {
    }
}
