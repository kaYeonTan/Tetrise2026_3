package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import kr.ac.jbnu.se.tetris.core.PlayerAction;

final class PausePanelScreen implements PanelScreen {
    @Override
    public void render(Graphics2D g, GamePanel p) {
        p.drawGame(g);
        p.drawPause(g);
    }

    @Override
    public boolean handleClick(GamePanel p) {
        if (p.clicked(GamePanel.PAUSE_BTN_X, GamePanel.PAUSE_Y_CONTINUE, GamePanel.PAUSE_BTN_W, GamePanel.PAUSE_BTN_H)) {
            if (p.isOnlineCoop && p.awaitingGuestCoopSave) {
                p.msg = "방장 저장에 이어 참가자 저장을 먼저 완료해 주세요.";
                p.msgTimer = 200;
            } else if (p.isOnlineCoop && p.coopBridge != null) {
                p.performCoopAction(p.localPlayerIndex, PlayerAction.RESUME, false);
            } else {
                p.resumeLocalPause();
            }
        } else if (p.clicked(GamePanel.PAUSE_BTN_X, GamePanel.PAUSE_Y_SAVE, GamePanel.PAUSE_BTN_W, GamePanel.PAUSE_BTN_H)) {
            save(p);
        } else if (p.clicked(GamePanel.PAUSE_BTN_X, GamePanel.PAUSE_Y_LOAD, GamePanel.PAUSE_BTN_W, GamePanel.PAUSE_BTN_H)) {
            if (p.isOnlineCoop && p.coopBridge != null && !p.coopBridge.isHost()) {
                p.msg = "방장만 불러올 수 있어요.";
                p.msgTimer = 180;
            } else if (p.isOnlineCoop && p.loginUser == null) {
                p.msg = "온라인 협동은 로그인 후 불러올 수 있어요.";
                p.msgTimer = 200;
            } else if (p.isTwoPlayer || p.isOnlineCoop) {
                p.openCoopSlotScreen(2, 6);
            }
        } else if (p.clicked(GamePanel.PAUSE_BTN_X, GamePanel.PAUSE_Y_SETTINGS, GamePanel.PAUSE_BTN_W, GamePanel.PAUSE_BTN_H)) {
            p.openSettings();
        } else if (p.clicked(GamePanel.PAUSE_BTN_X, GamePanel.PAUSE_Y_MAIN, GamePanel.PAUSE_BTN_W, GamePanel.PAUSE_BTN_H)) {
            exitToMain(p);
        }
        return true;
    }

    @Override
    public boolean handleKey(GamePanel p, KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (p.isOnlineCoop && p.coopBridge != null) {
                p.performCoopAction(p.localPlayerIndex, PlayerAction.RESUME, false);
            } else {
                p.resumeLocalPause();
            }
        }
        return true;
    }

    private void save(GamePanel p) {
        if (p.isOnlineCoop && p.loginUser == null) {
            p.msg = "온라인 협동 저장은 로그인 후 이용할 수 있어요.";
            p.msgTimer = 200;
        } else if (p.isOnlineCoop && p.coopBridge != null && !p.coopBridge.isHost()) {
            p.msg = "방장만 협동 저장을 시작할 수 있어요.";
            p.msgTimer = 180;
        } else if (p.isTwoPlayer || p.isOnlineCoop) {
            p.openCoopSlotScreen(1, 6);
        } else if (p.loginUser == null) {
            p.msg = "게스트는 1인 저장을 할 수 없어요. 로그인 후 이용해 주세요.";
            p.msgTimer = 180;
        } else if (p.saveFile == null && p.singleSaves().list(p.loginUser).size() >= 5) {
            p.msg = "저장 칸이 가득 찼어요(최대 5개). 메인에서 지운 뒤 저장하세요.";
            p.msgTimer = 180;
        } else if (p.singleSaves().save(p)) {
            p.msg = "저장했어요! [플레이]에서 이어할 수 있어요.";
            p.msgTimer = 180;
        } else {
            p.msg = "저장하지 못했어요.";
            p.msgTimer = 180;
        }
    }

    private void exitToMain(GamePanel p) {
        if (p.loginUser != null) {
            int r = JOptionPane.showOptionDialog(p, "저장하지 않은 진행은 사라져요.", "메인으로",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[]{"저장하고 나가기", "그냥 나가기", "취소"}, "취소");
            if (r == 0) {
                if (p.saveFile == null && p.singleSaves().list(p.loginUser).size() >= 5) {
                    JOptionPane.showMessageDialog(p, "저장 칸이 가득 찼어요(최대 5개).");
                    return;
                }
                p.singleSaves().save(p);
                p.screen = 0;
                p.stopBgm();
            } else if (r == 1) {
                p.screen = 0;
                p.stopBgm();
            }
        } else {
            int r = JOptionPane.showOptionDialog(p, "게스트 진행은 저장되지 않아요.\n메인 화면으로 갈까요?", "메인으로",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"나가기", "취소"}, "취소");
            if (r == 0) {
                p.screen = 0;
                p.stopBgm();
            }
        }
    }
}
