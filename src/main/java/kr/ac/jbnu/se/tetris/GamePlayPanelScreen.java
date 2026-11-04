package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import kr.ac.jbnu.se.tetris.core.PlayerAction;

final class GamePlayPanelScreen implements PanelScreen {
    @Override
    public void render(Graphics2D g, GamePanel p) {
        p.drawGame(g);
    }

    @Override
    public void update(GamePanel p) {
        p.update();
    }

    @Override
    public boolean handleClick(GamePanel p) {
        if (p.stageAdvancePrompt && p.isOnlineCoop) {
            if (!p.isStageReady(p.localPlayerIndex)
                    && p.clicked(GamePanel.STAGE_ADVANCE_BTN_X, GamePanel.STAGE_ADVANCE_BTN_Y,
                            GamePanel.STAGE_ADVANCE_BTN_W, GamePanel.STAGE_ADVANCE_BTN_H)) {
                p.performCoopAction(p.localPlayerIndex, PlayerAction.STAGE_READY, false);
            }
        }
        return true;
    }

    @Override
    public boolean handleKey(GamePanel p, KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE || code == GamePanel.keyPause) {
            if (!p.stageClear) {
                if (p.isOnlineCoop && p.coopBridge != null) {
                    p.performCoopAction(p.localPlayerIndex, PlayerAction.PAUSE, false);
                } else {
                    p.enterLocalPause("");
                }
            }
            return true;
        }
        if (p.stageClear || p.pressed.contains(code)) {
            return true;
        }
        p.pressed.add(code);
        int[] k = p.currentPrimaryKeys();
        if (p.useAEngine()) {
            p.aEngineBridge.handleKey(code, k);
            resetRepeat(p.players[0], code, k);
        } else if (p.controlsPlayer(0)) {
            applyKeys(p, 0, code, k);
        }
        if (p.isTwoPlayer && p.screen == 5 && !p.stageClear && p.controlsPlayer(1)) {
            applyKeys(p, 1, code, p.onlineCoopKeysForP2());
        }
        return true;
    }

    private static void resetRepeat(Player pl, int code, int[] k) {
        if (code == k[0]) {
            pl.leftCount = 0;
        } else if (code == k[1]) {
            pl.rightCount = 0;
        } else if (code == k[2]) {
            pl.downCount = 0;
        }
    }

    private static void applyKeys(GamePanel p, int index, int code, int[] k) {
        Player pl = p.players[index];
        PlayerAction action = KeyMapper.map(code, k).orElse(null);
        if (action == null) {
            return;
        }
        p.performCoopAction(index, action, false);
        if (action == PlayerAction.MOVE_LEFT) {
            pl.leftCount = 0;
        } else if (action == PlayerAction.MOVE_RIGHT) {
            pl.rightCount = 0;
        } else if (action == PlayerAction.SOFT_DROP) {
            pl.downCount = 0;
        }
    }
}
