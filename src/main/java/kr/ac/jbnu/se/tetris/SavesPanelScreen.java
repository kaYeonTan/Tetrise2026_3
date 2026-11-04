package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

final class SavesPanelScreen implements PanelScreen {
    @Override
    public void render(Graphics2D g, GamePanel p) {
        p.drawSaves(g);
    }

    @Override
    public boolean handleClick(GamePanel p) {
        if (p.loginUser == null && p.clicked(UiLayout.GO_LOGIN)) {
            new LoginFrame(p, false).setVisible(true);
            if (p.loginUser != null) {
                p.saveList = p.loadSaveList();
            }
        } else if (p.clicked(UiLayout.NEW_GAME)) {
            p.newGame(false);
        } else if (p.clicked(UiLayout.BACK)) {
            p.screen = 0;
        } else if (p.loginUser != null) {
            for (int i = 0; i < p.saveList.size() && i < UiLayout.MAX_SAVE_ROWS; i++) {
                if (p.clicked(UiLayout.resumeButton(i))) {
                    if (p.singleSaves().load(p.saveList.get(i)[0], p)) {
                        p.screen = 5;
                        p.pressed.clear();
                    } else {
                        JOptionPane.showMessageDialog(p, "저장 파일을 읽을 수 없어요.");
                    }
                    break;
                }
                if (p.clicked(UiLayout.deleteButton(i))) {
                    int r = JOptionPane.showOptionDialog(p, "스테이지 " + p.saveList.get(i)[2] + " · 점수 " + p.saveList.get(i)[3]
                                    + "\n이 저장을 지울까요? 되돌릴 수 없어요.", "저장 삭제",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"삭제", "취소"}, "취소");
                    if (r == 0) {
                        p.singleSaves().delete(p.saveList.get(i)[0]);
                        p.saveList = p.loadSaveList();
                    }
                    break;
                }
            }
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
