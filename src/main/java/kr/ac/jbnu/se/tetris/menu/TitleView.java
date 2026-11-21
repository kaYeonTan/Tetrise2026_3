package kr.ac.jbnu.se.tetris.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.TitleLayout;

/** 타이틀 화면. */
public class TitleView implements MenuView {

    private final GamePanel panel;
    private final PixelButton single = new PixelButton("1인 플레이", TitleLayout.SINGLE);
    private final PixelButton multi = new PixelButton("2인 플레이", TitleLayout.MULTI);
    private final PixelButton ranking = new PixelButton("랭킹", TitleLayout.RANKING);
    private final PixelButton settings = new PixelButton("환경설정", TitleLayout.SETTINGS);
    private final PixelButton quit = new PixelButton("게임 종료", TitleLayout.QUIT);
    private final PixelButton logout = new PixelButton("로그아웃", TitleLayout.LOGIN);
    private final PixelButton login = new PixelButton("로그인", TitleLayout.LOGIN);
    private final PixelButton signUp = new PixelButton("회원가입", TitleLayout.SIGN_UP);

    public TitleView(GamePanel panel) {
        this.panel = panel;
    }

    @Override
    public void paint(Graphics2D g) {
        panel.drawCastle(g, false);
        for (int cx : new int[]{640, 1114}) {
            for (int i = 0; i < 12; i++) {
                g.setColor(i % 2 == 0 ? new Color(0x3a3540) : new Color(0x1a171d));
                g.fillRect(cx, 36 + i * 6, 6, 6);
            }
            g.setColor(new Color(0x0f0d12));
            g.fillRect(cx - 6, 24, 18, 12);
        }
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(594, 124, 600, 486);
        if (panel.mapImage() != null) {
            g.drawImage(panel.mapImage(), 580, 110, 600, 486, null);
        }
        g.setFont(panel.fontBold.deriveFont(66f));
        panel.shadowText(g, "TETRIS", 84, 172, new Color(0xf0e2c8), new Color(0x6b2a1a), 5);
        g.setFont(panel.baseFont.deriveFont(22f));
        panel.shadowText(g, "다섯 성의 보스를 무너뜨려라", 90, 214, new Color(0xd9c29a), new Color(0x0c090d), 2);
        panel.drawButton(g, single, 1, 22f);
        panel.drawButton(g, multi, 0, 22f);
        panel.drawButton(g, ranking, 0, 22f);
        panel.drawButton(g, settings, 0, 22f);
        panel.drawButton(g, quit, 0, 18f);
        g.setFont(panel.baseFont.deriveFont(22f));
        String loginUser = panel.getLoginUser();
        if (loginUser != null) {
            panel.drawButton(g, logout, 2, 16.5f);
            panel.shadowText(g, loginUser + " 용사님, 모험을 떠나요!", 88, 672, new Color(0xe9c98f), new Color(0x0c090d), 2);
        } else {
            panel.drawButton(g, login, 2, 16.5f);
            panel.drawButton(g, signUp, 2, 16.5f);
            g.setFont(panel.baseFont.deriveFont(22f));
            panel.shadowText(g, "게스트로 플레이 중", 88, 672, new Color(0xd9c29a), new Color(0x0c090d), 2);
        }
        g.setFont(panel.fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x9a8f9f));
    }

    @Override
    public void click(int x, int y) {
        handleTitleClick();
    }

    @Override
    public void key(KeyEvent e) {
        // 타이틀 화면은 키 입력이 없다.
    }

    private void handleTitleClick() {
        if (panel.clicked(single)) {
            panel.reloadSaveList();
            panel.setScreen(GamePanel.SCREEN_SAVES);
        } else if (panel.clicked(multi)) {
            panel.promptTwoPlayerMode();
        } else if (panel.clicked(ranking)) {
            panel.loadRanking();
            panel.setScreen(GamePanel.SCREEN_RANKING);
        } else if (panel.clicked(settings)) {
            panel.openSettings();
        } else if (panel.clicked(quit)) {
            if (panel.confirmOrCancel("모험을 마치고 게임을 종료할까요?", "게임 종료", "종료")) {
                System.exit(0);
            }
        } else if (panel.clicked(login)) {
            if (panel.getLoginUser() == null) {
                panel.showLoginDialog(false);
            } else if (panel.confirmOrCancel("로그아웃할까요?", "로그아웃", "로그아웃")) {
                panel.setLoginUser(null);
            }
        } else if (panel.getLoginUser() == null && panel.clicked(signUp)) {
            panel.showLoginDialog(true);
        }
    }
}
