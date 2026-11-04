package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;

final class ResultPanelScreen implements PanelScreen {
    @Override
    public void render(Graphics2D g, GamePanel p) {
        p.drawGame(g);
        p.drawResult(g);
    }

    @Override
    public boolean handleClick(GamePanel p) {
        if (p.clicked(490, 470, 300, 54)) {
            p.screen = 0;
        }
        return true;
    }
}
