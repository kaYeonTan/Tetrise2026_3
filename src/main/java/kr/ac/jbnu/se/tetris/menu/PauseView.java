package kr.ac.jbnu.se.tetris.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.ScreenId;
import kr.ac.jbnu.se.tetris.core.PlayerAction;

/** 일시정지 화면. */
public class PauseView implements MenuView {

    private static final int BTN_X = 490;
    private static final int BTN_W = 300;
    private static final int BTN_H = 54;
    private static final int BTN_GAP = 18;
    private static final int Y_CONTINUE = 210;
    private static final int Y_SAVE = Y_CONTINUE + BTN_H + BTN_GAP;
    private static final int Y_LOAD = Y_SAVE + BTN_H + BTN_GAP;
    private static final int Y_SETTINGS = Y_LOAD + BTN_H + BTN_GAP;
    private static final int Y_MAIN = Y_SETTINGS + BTN_H + BTN_GAP;
    /** 일시정지 안내 문구 — 마지막 버튼(그림자 포함) 아래 */
    private static final int MSG_Y = Y_MAIN + BTN_H + 28;
    private static final int PANEL_H = MSG_Y - 120 + 24;

    private final GamePanel panel;
    private final PixelButton continueBtn = button("계속하기", Y_CONTINUE);
    private final PixelButton saveBtn = button("저장하기", Y_SAVE);
    private final PixelButton loadBtn = button("불러오기", Y_LOAD);
    private final PixelButton settingsBtn = button("환경설정", Y_SETTINGS);
    private final PixelButton mainBtn = button("메인으로", Y_MAIN);

    public PauseView(GamePanel panel) {
        this.panel = panel;
    }

    private static PixelButton button(String label, int y) {
        return new PixelButton(label, new Rectangle(BTN_X, y, BTN_W, BTN_H));
    }

    @Override
    public void paint(Graphics2D g) {
        panel.drawGame(g);
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        panel.drawPanel(g, 420, 120, 440, PANEL_H);
        g.setFont(panel.fontBold.deriveFont(33f));
        panel.centerText(g, "일시정지", 180, new Color(0x1a1216), new Color(0xefe0bf));
        panel.drawButton(g, continueBtn, 1, 22f);
        panel.drawButton(g, saveBtn, 0, 22f);
        if (panel.isTwoPlayer || panel.isOnlineCoop) {
            panel.drawButton(g, loadBtn, 0, 22f);
        }
        panel.drawButton(g, settingsBtn, 0, 22f);
        panel.drawButton(g, panel.isOnlineCoop ? button("로비로", Y_MAIN) : mainBtn, 0, 22f);
        g.setFont(panel.fontSmall.deriveFont(13.5f));
        String message = panel.msgTimer > 0 ? panel.msg : "";
        panel.centerPlainText(g, message, 640, MSG_Y, new Color(0x1a1216));
    }

    @Override
    public void click(int x, int y) {
        handlePauseClick();
    }

    @Override
    public void key(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (panel.isOnlineCoop && panel.coopBridge != null) {
                panel.performCoopAction(panel.localPlayerIndex, PlayerAction.RESUME, false);
            } else {
                panel.resumeLocalPause();
            }
        }
    }

    private void handlePauseClick() {
        if (panel.clicked(continueBtn)) {
            if (panel.isOnlineCoop && panel.isAwaitingGuestCoopSave()) {
                panel.showMsg("방장 저장에 이어 참가자 저장을 먼저 완료해 주세요.", 200);
            } else if (panel.isOnlineCoop && panel.coopBridge != null) {
                panel.performCoopAction(panel.localPlayerIndex, PlayerAction.RESUME, false);
            } else {
                panel.resumeLocalPause();
            }
        } else if (panel.clicked(saveBtn)) {
            handlePauseSave();
        } else if (panel.clicked(loadBtn)) {
            handlePauseLoad();
        } else if (panel.clicked(settingsBtn)) {
            panel.openSettings();
        } else if (panel.clicked(mainBtn)) {
            handlePauseToMain();
        }
    }

    private void handlePauseSave() {
        String loginUser = panel.getLoginUser();
        if (panel.isOnlineCoop && loginUser == null) {
            panel.showMsg("온라인 협동 저장은 로그인 후 이용할 수 있어요.", 200);
        } else if (panel.isOnlineCoop && panel.coopBridge != null && !panel.coopBridge.isHost()) {
            panel.showMsg("방장만 협동 저장을 시작할 수 있어요.", 180);
        } else if (panel.isTwoPlayer || panel.isOnlineCoop) {
            panel.openCoopSlotScreen(1, ScreenId.PAUSE);
        } else if (loginUser == null) {
            panel.showMsg("게스트는 1인 저장을 할 수 없어요. 로그인 후 이용해 주세요.", 180);
        } else if (panel.saveFile == null && panel.saves().getSaveList(loginUser).size() >= 5) {
            panel.showMsg("저장 칸이 가득 찼어요(최대 5개). 메인에서 지운 뒤 저장하세요.", 180);
        } else if (panel.saves().saveGame(panel)) {
            panel.showMsg("저장했어요! [플레이]에서 이어할 수 있어요.", 180);
        } else {
            panel.showMsg("저장하지 못했어요.", 180);
        }
    }

    private void handlePauseLoad() {
        if (panel.isOnlineCoop && panel.coopBridge != null && !panel.coopBridge.isHost()) {
            panel.showMsg("방장만 불러올 수 있어요.", 180);
        } else if (panel.isOnlineCoop && panel.getLoginUser() == null) {
            panel.showMsg("온라인 협동은 로그인 후 불러올 수 있어요.", 200);
        } else if (panel.isTwoPlayer || panel.isOnlineCoop) {
            panel.openCoopSlotScreen(2, ScreenId.PAUSE);
        }
    }

    private void handlePauseToMain() {
        if (panel.isOnlineCoop) {
            int r = JOptionPane.showOptionDialog(panel,
                    "온라인 협동을 종료하고 로비로 돌아갈까요?\n(이 방은 목록에서 사라져요.)", "로비로",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[]{"로비로", "취소"}, "취소");
            if (r == 0) panel.returnToOnlineLobby(null);
            return;
        }
        String loginUser = panel.getLoginUser();
        if (loginUser == null) {
            int r = JOptionPane.showOptionDialog(panel, "게스트 진행은 저장되지 않아요.\n메인 화면으로 갈까요?", "메인으로",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"나가기", "취소"}, "취소");
            if (r == 0) {
                panel.leaveToTitle();
            }
            return;
        }
        int r = JOptionPane.showOptionDialog(panel, "저장하지 않은 진행은 사라져요.", "메인으로",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"저장하고 나가기", "그냥 나가기", "취소"}, "취소");
        if (r == 0) {
            if (panel.saveFile == null && panel.saves().getSaveList(loginUser).size() >= 5) {
                JOptionPane.showMessageDialog(panel, "저장 칸이 가득 찼어요(최대 5개).");
                return;
            }
            panel.saves().saveGame(panel);
            panel.leaveToTitle();
        } else if (r == 1) {
            panel.leaveToTitle();
        }
    }
}
