package kr.ac.jbnu.se.tetris.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.jbnu.se.tetris.Art;
import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.UiLayout;

/** 저장 목록 화면. */
public class SavesView implements MenuView {

    private static final String[] SAVE_STAGE_NAMES = {"", "불타는 사막", "고립된 섬", "고요한 초원", "얼어붙은 설원", "하늘 위 마왕성"};

    private final GamePanel panel;
    private final PixelButton goLogin = new PixelButton("로그인하러 가기", UiLayout.GO_LOGIN);
    private final PixelButton newGame = new PixelButton("+ 새 게임", UiLayout.NEW_GAME);
    private final PixelButton back = new PixelButton("← 뒤로", UiLayout.BACK);
    private final PixelButton[] resume = new PixelButton[UiLayout.MAX_SAVE_ROWS];
    private final PixelButton[] delete = new PixelButton[UiLayout.MAX_SAVE_ROWS];

    public SavesView(GamePanel panel) {
        this.panel = panel;
        for (int i = 0; i < UiLayout.MAX_SAVE_ROWS; i++) {
            resume[i] = new PixelButton("이어하기", UiLayout.resumeButton(i));
            delete[i] = new PixelButton("삭제", UiLayout.deleteButton(i));
        }
    }

    @Override
    public void paint(Graphics2D g) {
        String loginUser = panel.getLoginUser();
        panel.drawCastle(g, true);
        g.setFont(panel.fontBold.deriveFont(44f));
        panel.centerText(g, "저장된 모험", 92, new Color(0xf0e2c8), new Color(0x6b2a1a));
        g.setFont(panel.baseFont.deriveFont(16.5f));
        g.setColor(new Color(0xd9c29a));
        String sub = loginUser == null ? "게스트로 플레이 중" : loginUser + " 용사님의 저장 목록 (최대 5개)";
        g.drawString(sub, 640 - g.getFontMetrics().stringWidth(sub) / 2, 126);
        if (loginUser == null || panel.saveList().isEmpty()) {
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRect(250, 250, 780, 250);
            g.setFont(panel.fontBold.deriveFont(22f));
            String t1 = loginUser == null ? "게스트는 게임을 저장할 수 없어요." : "이전 플레이 기록이 없어요.";
            String t2 = loginUser == null ? "로그인하면 저장한 모험을 이어서 할 수 있어요. 새 게임은 바로 시작할 수 있어요."
                    : "아래 [+ 새 게임]으로 첫 모험을 시작해 보세요!";
            panel.centerText(g, t1, 330, new Color(0xf0e2c8), new Color(0x0c090d));
            g.setFont(panel.baseFont.deriveFont(16.5f));
            g.setColor(new Color(0xd9c29a));
            g.drawString(t2, 640 - g.getFontMetrics().stringWidth(t2) / 2, 372);
            if (loginUser == null) {
                panel.drawButton(g, goLogin, 0, 16.5f);
            }
        } else {
            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            for (int i = 0; i < panel.saveList().size() && i < 5; i++) {
                drawSaveRow(g, i, panel.saveList().get(i), df);
            }
        }
        panel.drawButton(g, newGame, 1, 22f);
        panel.drawButton(g, back, 2, 16.5f);
    }

    private void drawSaveRow(Graphics2D g, int index, String[] save, SimpleDateFormat dateFormat) {
        int y = 146 + index * 88;
        int saveStage = Integer.parseInt(save[2]);
        g.setColor(new Color(0x1a1216));
        g.fillRect(250, y, 780, 80);
        g.setColor(new Color(0xd9c29a));
        g.fillRect(254, y + 4, 772, 72);
        g.setColor(new Color(0xefe0bf));
        g.fillRect(254, y + 4, 772, 3);
        g.drawImage(Art.background(saveStage), 262, y + 10, 262 + 110, y + 70, 260, 90, 1020, 630, null);
        g.setColor(new Color(0x1a1216));
        g.drawRect(262, y + 10, 110, 60);
        g.setFont(panel.fontBold.deriveFont(22f));
        g.drawString(GamePanel.STAGE_LABEL_KO + saveStage + " · " + SAVE_STAGE_NAMES[saveStage], 390, y + 32);
        g.setFont(panel.baseFont.deriveFont(16.5f));
        g.setColor(new Color(0x4a3a2a));
        g.drawString((save[4].equals("2") ? "단일 PC 2인" : "1인 플레이")
                + "  ·  점수 " + String.format("%,d", Integer.parseInt(save[3])), 390, y + 54);
        g.drawString("마지막 플레이  " + dateFormat.format(new Date(Long.parseLong(save[1]))), 390, y + 74);
        panel.drawButton(g, resume[index], 1, 16.5f);
        panel.drawButton(g, delete[index], 0, 16.5f);
    }

    @Override
    public void click(int x, int y) {
        handleSavesClick();
    }

    @Override
    public void key(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            panel.setScreen(GamePanel.SCREEN_TITLE);
        }
    }

    private void handleSavesClick() {
        if (panel.getLoginUser() == null && panel.clicked(goLogin)) {
            panel.showLoginDialog(false);
            if (panel.getLoginUser() != null) {
                panel.reloadSaveList();
            }
        } else if (panel.clicked(newGame)) {
            panel.newGame(false);
        } else if (panel.clicked(back)) {
            panel.setScreen(GamePanel.SCREEN_TITLE);
        } else if (panel.getLoginUser() != null) {
            handleSaveRowClick();
        }
    }

    private void handleSaveRowClick() {
        int visibleRows = Math.min(panel.saveList().size(), UiLayout.MAX_SAVE_ROWS);
        for (int row = 0; row < visibleRows; row++) {
            if (panel.clicked(resume[row])) {
                panel.resumeSave(panel.saveList().get(row)[0]);
                return;
            }
            if (panel.clicked(delete[row])) {
                panel.deleteSaveAfterConfirm(panel.saveList().get(row));
                return;
            }
        }
    }
}
