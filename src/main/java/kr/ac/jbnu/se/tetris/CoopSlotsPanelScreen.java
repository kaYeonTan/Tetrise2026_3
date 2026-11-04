package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;

/** 협동 저장/불러오기 슬롯 선택 화면 (screen 8). */
public class CoopSlotsPanelScreen implements PanelScreen {

    @Override
    public void render(Graphics2D g, GamePanel p) {
        p.drawTitle(g);
        p.drawCoopSlots(g);
    }

    @Override
    public boolean handleClick(GamePanel p) {
        if (p.clicked(490, 560, 300, 48)) {
            cancel(p);
            return true;
        }
        for (int i = 0; i < 3; i++) {
            if (p.clicked(340, 220 + i * 92, 600, 72)) {
                p.pickCoopSlot(i + 1);
                return true;
            }
        }
        return false;
    }

    private void cancel(GamePanel p) {
        if (p.coopSlotMode == 3) {
            p.msg = "참가자 저장을 완료해야 합니다.";
            p.msgTimer = 180;
            return;
        }
        if (p.coopSlotMode == 1 && p.isOnlineCoop && p.coopBridge != null && p.coopBridge.isHost()) {
            p.coopBridge.notifyGuestSaveUiCancelled();
        }
        if (p.coopSlotMode == 1 || p.coopSlotMode == 3) {
            p.screen = 6;
        } else {
            p.screen = p.coopSlotReturnScreen;
        }
    }
}
