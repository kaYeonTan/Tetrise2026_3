package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

final class RankingPanelScreen implements PanelScreen {
    @Override
    public void render(Graphics2D g, GamePanel p) {
        p.drawRanking(g);
    }

    @Override
    public boolean handleClick(GamePanel p) {
        if (p.clicked(UiLayout.RANK_SCORE_TAB)) {
            p.rankTab = 0;
            p.loadRanking();
        } else if (p.clicked(UiLayout.RANK_STAGE_TAB)) {
            p.rankTab = 1;
            p.loadRanking();
        } else if (p.clicked(UiLayout.BACK)) {
            p.screen = 0;
        }
        return true;
    }

    @Override
    public boolean handleKey(GamePanel p, KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            p.screen = 0;
        }
        return true;
    }
}
