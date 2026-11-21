package kr.ac.jbnu.se.tetris.menu;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/** 메뉴 화면 하나의 그리기·마우스·키 처리를 맡는다. */
public interface MenuView {

    void paint(Graphics2D g);

    void click(int x, int y);

    void key(KeyEvent e);
}
