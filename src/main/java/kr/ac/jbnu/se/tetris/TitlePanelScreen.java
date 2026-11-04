package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JOptionPane;

final class TitlePanelScreen implements PanelScreen {
    @Override
    public void render(Graphics2D g, GamePanel p) {
        p.drawTitle(g);
    }

    @Override
    public boolean handleClick(GamePanel p) {
        if (p.clicked(TitleLayout.SINGLE)) {
            p.saveList = p.loadSaveList();
            p.screen = 1;
        } else if (p.clicked(TitleLayout.MULTI)) {
            p.promptTwoPlayerMode();
        } else if (p.clicked(TitleLayout.RANKING)) {
            p.loadRanking();
            p.screen = 3;
        } else if (p.clicked(TitleLayout.SETTINGS)) {
            p.settingFrom = 0;
            p.editKeys = KeyBindings.current();
            p.setTab = 0;
            p.captureRow = -1;
            p.setMsg = "";
            p.screen = 4;
        } else if (p.clicked(TitleLayout.QUIT)) {
            int r = JOptionPane.showOptionDialog(p, "모험을 마치고 게임을 종료할까요?", "게임 종료",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"종료", "취소"}, "취소");
            if (r == 0) {
                System.exit(0);
            }
        } else if (p.loginUser == null && p.clicked(TitleLayout.LOGIN)) {
            new LoginFrame(p, false).setVisible(true);
        } else if (p.loginUser == null && p.clicked(TitleLayout.SIGN_UP)) {
            new LoginFrame(p, true).setVisible(true);
        } else if (p.loginUser != null && p.clicked(TitleLayout.LOGIN)) {
            int r = JOptionPane.showOptionDialog(p, "로그아웃할까요?", "로그아웃",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"로그아웃", "취소"}, "취소");
            if (r == 0) {
                p.loginUser = null;
            }
        }
        return true;
    }
}
