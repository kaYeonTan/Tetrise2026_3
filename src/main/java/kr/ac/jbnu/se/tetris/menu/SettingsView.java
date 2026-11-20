package kr.ac.jbnu.se.tetris.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.KeySettings;
import kr.ac.jbnu.se.tetris.ScreenId;
import kr.ac.jbnu.se.tetris.SettingsLayout;

/** 환경설정(키 설정) 화면. 편집 중인 상태(draft)는 이 뷰가 가진다. */
public class SettingsView implements MenuView {

    private final GamePanel panel;
    private final PixelButton[] tabs = new PixelButton[SettingsLayout.TAB_COUNT];
    private final PixelButton reset = new PixelButton("기본값 복원", SettingsLayout.RESET);
    private final PixelButton save = new PixelButton("저장", SettingsLayout.SAVE);
    private final PixelButton close = new PixelButton("닫기", SettingsLayout.CLOSE);

    private ScreenId settingFrom = ScreenId.TITLE;
    private int setTab = 0;
    private int captureRow = -1;
    private KeySettings editKeys;
    private String setMsg = "";
    private boolean setMsgError = false;

    public SettingsView(GamePanel panel) {
        this.panel = panel;
        String[] tabNames = {"1인 모드", "2인 P1", "2인 P2", "공통"};
        for (int t = 0; t < tabs.length; t++) {
            tabs[t] = new PixelButton(tabNames[t], SettingsLayout.tab(t));
        }
    }

    public void open() {
        settingFrom = panel.getScreen();
        editKeys = KeySettings.current();
        setTab = settingFrom == GamePanel.SCREEN_PAUSE && panel.isTwoPlayer ? 1 : 0;
        captureRow = -1;
        setMsg = "";
        panel.setScreen(GamePanel.SCREEN_SETTINGS);
    }

    @Override
    public void paint(Graphics2D g) {
        panel.drawSettingsBackdrop(g, settingFrom);
        g.setColor(new Color(8, 6, 10, 170));
        g.fillRect(0, 0, 1280, 720);
        panel.drawPanel(g, 210, 50, 860, 620);
        g.setFont(panel.fontBold.deriveFont(33f));
        panel.shadowText(g, "환경설정 · 키 설정", 250, 108, new Color(0x1a1216), new Color(0xefe0bf), 2);
        for (int t = 0; t < 4; t++) {
            panel.drawButton(g, tabs[t], t == setTab ? 1 : 0, 16.5f);
        }
        for (int r = 0; r < editKeys.rowCount(setTab); r++) {
            int y = 190 + r * 48;
            g.setFont(panel.font.deriveFont(22f));
            g.setColor(new Color(0x1a1216));
            g.drawString(KeySettings.actionName(setTab, r), 330, y + 27);
            String label = captureRow == r ? "키를 누르세요…" : GamePanel.keyName(editKeys.get(setTab, r));
            panel.drawButton(g, new PixelButton(label, SettingsLayout.keyButton(r)), captureRow == r ? 1 : 0, 16.5f);
            g.setColor(new Color(0xb89c6c));
            g.fillRect(310, y + 42, 600, 2);
        }

        g.setFont(panel.fontSmall.deriveFont(13.5f));
        g.setColor(new Color(0x5a4a3a));
        g.drawString("키 설정은 이 컴퓨터에 저장되고 모든 사용자에게 적용돼요.", 250, 540);
        g.setFont(panel.font.deriveFont(16.5f));
        g.setColor(setMsgError ? new Color(0xa8352a) : new Color(0x3a5a2a));
        g.drawString(setMsg, 250, 568);
        panel.drawButton(g, reset, 0, 16.5f);
        panel.drawButton(g, save, 1, 16.5f);
        panel.drawButton(g, close, 0, 16.5f);
    }

    @Override
    public void click(int x, int y) {
        handleSettingsClick();
    }

    @Override
    public void key(KeyEvent e) {
        handleSettingsKey(e.getKeyCode());
    }

    private void handleSettingsClick() {
        for (int tab = 0; tab < SettingsLayout.TAB_COUNT; tab++) {
            if (panel.clicked(tabs[tab])) {
                setTab = tab;
                captureRow = -1;
            }
        }
        for (int row = 0; row < editKeys.rowCount(setTab); row++) {
            if (panel.clicked(SettingsLayout.keyButton(row))) {
                captureRow = row;
                setMsg = "새 키를 누르세요. 취소하려면 Esc를 누르세요.";
                setMsgError = false;
            }
        }
        if (panel.clicked(reset)) {
            editKeys = KeySettings.defaults();
            captureRow = -1;
            setMsg = "기본 키 배치로 되돌렸어요. 저장을 눌러야 적용돼요.";
            setMsgError = false;
        } else if (panel.clicked(save)) {
            saveSettings();
        } else if (panel.clicked(close)) {
            closeSettings();
        }
    }

    private void handleSettingsKey(int code) {
        if (captureRow < 0) {
            if (code == KeyEvent.VK_ESCAPE) {
                closeSettings();
            }
            return;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            captureRow = -1;
            setMsg = "변경을 취소했어요.";
            setMsgError = false;
            return;
        }
        if (KeySettings.isIgnoredKey(code)) {
            return;
        }
        String used = editKeys.findConflict(setTab, captureRow, code);
        if (used != null) {
            setMsg = "'" + GamePanel.keyName(code) + "' 키는 이미 [" + used + "]에 쓰이고 있어요.";
            setMsgError = true;
        } else {
            editKeys.set(setTab, captureRow, code);
            setMsg = "[" + KeySettings.actionName(setTab, captureRow) + "] → " + GamePanel.keyName(code) + "  (저장을 눌러야 적용돼요)";
            setMsgError = false;
        }
        captureRow = -1;
    }

    private void saveSettings() {
        editKeys.applyToGame();
        panel.keys().saveKeys();
        setMsg = "저장했어요! 바로 게임에 적용돼요.";
        setMsgError = false;
    }

    private void closeSettings() {
        boolean changed = !editKeys.equals(KeySettings.current());
        if (changed) {
            int r = JOptionPane.showOptionDialog(panel, "바꾼 키 설정을 저장하지 않았어요. 어떻게 할까요?", "저장하지 않은 변경",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"저장", "버리기", "취소"}, "취소");
            if (r == 0) {
                saveSettings();
            } else if (r != 1) {
                return;
            }
        }
        panel.setScreen(settingFrom);
        panel.clearPressed();
    }
}
