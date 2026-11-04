package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

final class SettingsPanelScreen implements PanelScreen {
    @Override
    public void render(Graphics2D g, GamePanel p) {
        if (p.settingFrom == 0) {
            p.drawTitle(g);
        } else {
            p.drawGame(g);
        }
        p.drawSettings(g);
    }

    @Override
    public boolean handleClick(GamePanel p) {
        for (int t = 0; t < SettingsLayout.TAB_COUNT; t++) {
            if (p.clicked(SettingsLayout.tab(t))) {
                p.setTab = t;
                p.captureRow = -1;
            }
        }
        for (int r = 0; r < p.editKeys.rowCount(p.setTab); r++) {
            if (p.clicked(SettingsLayout.keyButton(r))) {
                p.captureRow = r;
                p.setMsg = "새 키를 누르세요. 취소하려면 Esc를 누르세요.";
                p.setMsgError = false;
            }
        }
        if (p.clicked(SettingsLayout.RESET)) {
            p.editKeys = KeyBindings.defaults();
            p.captureRow = -1;
            p.setMsg = "기본 키 배치로 되돌렸어요. 저장을 눌러야 적용돼요.";
            p.setMsgError = false;
        } else if (p.clicked(SettingsLayout.SAVE)) {
            p.saveSettings();
        } else if (p.clicked(SettingsLayout.CLOSE)) {
            p.closeSettings();
        }
        return true;
    }

    @Override
    public boolean handleKey(GamePanel p, KeyEvent e) {
        int code = e.getKeyCode();
        if (p.captureRow >= 0) {
            captureKey(p, code);
            return true;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            p.closeSettings();
        }
        return true;
    }

    private void captureKey(GamePanel p, int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            p.captureRow = -1;
            p.setMsg = "변경을 취소했어요.";
            p.setMsgError = false;
            return;
        }
        if (KeyBindings.isIgnoredKey(code)) {
            return;
        }
        String used = p.editKeys.findConflict(p.setTab, p.captureRow, code);
        if (used != null) {
            p.setMsg = "'" + GamePanel.keyName(code) + "' 키는 이미 [" + used + "]에 쓰이고 있어요.";
            p.setMsgError = true;
        } else {
            p.editKeys.set(p.setTab, p.captureRow, code);
            p.setMsg = "[" + KeyBindings.actionName(p.setTab, p.captureRow) + "] → " + GamePanel.keyName(code)
                    + "  (저장을 눌러야 적용돼요)";
            p.setMsgError = false;
        }
        p.captureRow = -1;
    }
}
