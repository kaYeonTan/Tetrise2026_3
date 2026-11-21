package kr.ac.jbnu.se.tetris.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.RecordEntry;
import kr.ac.jbnu.se.tetris.UiLayout;

/** 랭킹 화면. */
public class RankingView implements MenuView {

    private static final String[] RANK_STAGE_NAMES = {"", "사막", "섬", "초원", "설원", "마왕성"};
    private static final Color[] RANK_MEDAL_COLORS = {new Color(0xf2c14e), new Color(0xcfd6de), new Color(0xd08a4a)};

    private final GamePanel panel;
    private final PixelButton scoreTab = new PixelButton("점수 랭킹", UiLayout.RANK_SCORE_TAB);
    private final PixelButton stageTab = new PixelButton("스테이지 랭킹", UiLayout.RANK_STAGE_TAB);
    private final PixelButton back = new PixelButton("← 뒤로", UiLayout.BACK);

    public RankingView(GamePanel panel) {
        this.panel = panel;
    }

    @Override
    public void paint(Graphics2D g) {
        panel.drawCastle(g, true);
        g.setFont(panel.fontBold.deriveFont(44f));
        panel.centerText(g, "명예의 전당", 92, new Color(0xf0e2c8), new Color(0x6b2a1a));
        panel.drawButton(g, scoreTab, panel.rankTab() == 0 ? 1 : 0, 16.5f);
        panel.drawButton(g, stageTab, panel.rankTab() == 1 ? 1 : 0, 16.5f);
        g.setFont(panel.font.deriveFont(16.5f));
        g.setColor(new Color(0xd9c29a));
        g.drawString(panel.rankTab() == 0 ? "플레이어별 최고 점수 순" : "가장 멀리 간 스테이지 순 · 같으면 점수 순", 650, 180);
        g.setColor(new Color(0x1a1216));
        g.fillRect(170, 214, 940, 456);
        g.setColor(new Color(0x221b24));
        g.fillRect(174, 218, 932, 448);
        String[] heads = {"순위", "닉네임", "점수", "도달 스테이지", "인원", "날짜"};
        int[] colX = {194, 280, 550, 730, 900, 1010};
        g.setFont(panel.fontBold.deriveFont(16.5f));
        g.setColor(new Color(0xe9c98f));
        for (int i = 0; i < 6; i++) {
            g.drawString(heads[i], colX[i], 246);
        }
        g.setColor(new Color(0x4a3f52));
        g.fillRect(182, 258, 916, 3);
        if (panel.rankList().isEmpty()) {
            g.setFont(panel.font.deriveFont(22f));
            panel.centerText(g, "아직 기록이 없어요. 첫 번째 용사가 되어 보세요!", 450, new Color(0xf0e2c8), new Color(0x0c090d));
        }
        SimpleDateFormat df = new SimpleDateFormat("MM.dd");
        for (int i = 0; i < panel.rankList().size() && i < 10; i++) {
            drawRankRow(g, i, panel.rankList().get(i), df);
        }
        panel.drawButton(g, back, 2, 16.5f);
    }

    private void drawRankRow(Graphics2D g, int index, RecordEntry record, SimpleDateFormat dateFormat) {
        int y = 266 + index * 40;
        boolean isMine = record.nick().equals(panel.getLoginUser());
        if (isMine) {
            g.setColor(new Color(0x5a3a2a));
            g.fillRect(180, y, 920, 36);
        } else if (index % 2 == 1) {
            g.setColor(new Color(0x2a222d));
            g.fillRect(180, y, 920, 36);
        }
        if (index < 3) {
            g.setColor(RANK_MEDAL_COLORS[index]);
            g.fillRect(192, y + 6, 26, 24);
            g.setColor(new Color(0x1a1216));
            g.setFont(panel.fontBold.deriveFont(16.5f));
            g.drawString("" + (index + 1), 200, y + 24);
        } else {
            g.setColor(new Color(0x9a8f9f));
            g.setFont(panel.font.deriveFont(16.5f));
            g.drawString("" + (index + 1), 200, y + 24);
        }
        g.setFont(panel.font.deriveFont(22f));
        g.setColor(isMine ? new Color(0xe9c98f) : new Color(0xf0e2c8));
        g.drawString(record.nick() + (isMine ? " (나)" : ""), 280, y + 26);
        g.setColor(new Color(0xf0e2c8));
        g.drawString(String.format("%,d", record.score()), 550, y + 26);
        g.setFont(panel.font.deriveFont(16.5f));
        if (record.clear()) {
            g.setColor(new Color(0xe9c98f));
            g.drawString("전체 클리어", 730, y + 24);
        } else {
            g.setColor(new Color(0xd9c29a));
            g.drawString(record.stage() + " · " + RANK_STAGE_NAMES[record.stage()], 730, y + 24);
        }
        g.setColor(new Color(0xd9c29a));
        g.drawString(record.players() + "인", 900, y + 24);
        g.setColor(new Color(0x9a8f9f));
        g.drawString(dateFormat.format(new Date(record.timeMillis())), 1010, y + 24);
    }

    @Override
    public void click(int x, int y) {
        handleRankingClick();
    }

    @Override
    public void key(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            panel.setScreen(GamePanel.SCREEN_TITLE);
        }
    }

    private void handleRankingClick() {
        if (panel.clicked(scoreTab)) {
            panel.selectRankTab(0);
        } else if (panel.clicked(stageTab)) {
            panel.selectRankTab(1);
        } else if (panel.clicked(back)) {
            panel.setScreen(GamePanel.SCREEN_TITLE);
        }
    }
}
