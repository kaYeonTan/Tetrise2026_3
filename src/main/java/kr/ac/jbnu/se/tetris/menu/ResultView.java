package kr.ac.jbnu.se.tetris.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import kr.ac.jbnu.se.tetris.GamePanel;

/** 게임 결과 화면. */
public class ResultView implements MenuView {

    private final GamePanel panel;
    private final PixelButton mainBtn = new PixelButton("메인으로", new Rectangle(490, 470, 300, 54));

    public ResultView(GamePanel panel) {
        this.panel = panel;
    }

    @Override
    public void paint(Graphics2D g) {
        panel.drawGame(g);
        boolean allClear = panel.isAllClear();
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        panel.drawPanel(g, 390, 170, 500, 380);
        g.setFont(panel.fontBold.deriveFont(44f));
        panel.centerText(g, allClear ? "ALL CLEAR!" : "GAME OVER", 240, allClear ? new Color(0x6b2a1a) : new Color(0x1a1216), new Color(0xefe0bf));
        g.setFont(panel.baseFont.deriveFont(22f));
        Color textColor = new Color(0x1a1216);
        String summary = allClear ? "다섯 보스를 모두 쓰러뜨렸어요!" : GamePanel.STAGE_LABEL_KO + panel.stage + "에서 쓰러졌어요.";
        panel.centerPlainText(g, summary, 640, 300, textColor);
        String scoreText = panel.isTwoPlayer
                ? "P1 " + String.format("%,d", panel.p1Score) + "  ·  P2 " + String.format("%,d", panel.p2Score)
                : "점수 " + String.format("%,d", panel.p1Score);
        panel.centerPlainText(g, scoreText, 640, 344, textColor);
        if (panel.isTwoPlayer) {
            panel.centerPlainText(g, "합계 " + String.format("%,d", panel.p1Score + panel.p2Score), 640, 380, textColor);
        }
        g.setFont(panel.baseFont.deriveFont(16.5f));
        panel.centerPlainText(g, panel.resultMsg(), 640, 430, new Color(0x3a5a2a));
        panel.drawButton(g, panel.isOnlineCoop ? new PixelButton("로비로", mainBtn.bounds()) : mainBtn, 1, 22f);
    }

    @Override
    public void click(int x, int y) {
        handleResultClick();
    }

    @Override
    public void key(KeyEvent e) {
        // 결과 화면은 키 입력이 없다.
    }

    private void handleResultClick() {
        if (panel.clicked(mainBtn)) {
            if (panel.isOnlineCoop) panel.returnToOnlineLobby(null);
            else panel.setScreen(GamePanel.SCREEN_TITLE);
        }
    }
}
