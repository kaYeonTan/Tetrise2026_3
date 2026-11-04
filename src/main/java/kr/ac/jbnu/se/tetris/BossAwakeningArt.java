package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Graphics2D;

/** 보스 각성 연출의 스테이지별 그림. 상태는 갖지 않고 경과 프레임(passed)만 받아 그린다. */
final class BossAwakeningArt {
    private static final int FINAL_STAGE = 5;
    private static final int FINAL_STAGE_FRAMES = 75;
    private static final int DEFAULT_FRAMES = 70;
    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;

    private BossAwakeningArt() {
    }

    static int duration(int stage) {
        return stage == FINAL_STAGE ? FINAL_STAGE_FRAMES : DEFAULT_FRAMES;
    }

    static void draw(Graphics2D g, int stage, int passed, int bossX) {
        switch (stage) {
            case 1 -> drawDesert(g, passed, bossX);
            case 2 -> drawIsland(g, passed, bossX);
            case 3 -> drawMeadow(g, passed);
            case 4 -> drawSnow(g, passed);
            case FINAL_STAGE -> drawCastle(g, passed);
            default -> {
            }
        }
    }

    private static void drawDesert(Graphics2D g, int passed, int bossX) {
        for (int i = 0; i < 14; i++) {
            int x = bossX - 40 - i * 72 + passed * 9;
            int y = 610 - (i * 53 + passed * 5) % 520;
            g.setColor(new Color(75, 112, 46, 210));
            g.fillPolygon(new int[]{x, x + 18, x + 5}, new int[]{y, y + 5, y + 13}, 3);
            g.setColor(new Color(220, 204, 120, 220));
            g.drawLine(x + 2, y + 2, x + 15, y + 5);
        }
    }

    private static void drawIsland(Graphics2D g, int passed, int bossX) {
        for (int wave = 0; wave < 3; wave++) {
            int waveY = 580 - wave * 90 - (passed * 4) % 80;
            g.setColor(new Color(88, 190, 231, 120));
            for (int x = 0; x < SCREEN_WIDTH; x += 24) {
                int startY = waveY + (int) (Math.sin((x + passed * 18) * 0.05) * 11);
                int endY = waveY + (int) (Math.sin((x + 24 + passed * 18) * 0.05) * 11);
                g.drawLine(x, startY, x + 24, endY);
            }
        }
        g.setColor(new Color(245, 250, 255, 220));
        int boltX = bossX - 20 + (passed % 3) * 12;
        int boltY = 110;
        for (int segment = 0; segment < 6; segment++) {
            int nextX = boltX + (segment % 2 == 0 ? -18 : 20);
            g.drawLine(boltX, boltY, nextX, boltY + 42);
            boltX = nextX;
            boltY += 42;
        }
    }

    private static void drawMeadow(Graphics2D g, int passed) {
        for (int i = 0; i < 38; i++) {
            int x = Math.floorMod(i * 97 + passed * (i % 3 + 2) * 5, SCREEN_WIDTH);
            int y = Math.floorMod(i * 47 + passed * (i % 4 + 3) * 4, 610);
            g.setColor(i % 3 == 0 ? new Color(211, 170, 70, 200)
                    : new Color(92, 151, 70, 210));
            g.fillPolygon(new int[]{x, x + 10, x + 4, x - 5},
                    new int[]{y, y + 4, y + 12, y + 7}, 4);
        }
    }

    private static void drawSnow(Graphics2D g, int passed) {
        for (int i = 0; i < 44; i++) {
            int x = Math.floorMod(i * 83 + passed * (i % 4 + 2) * 4, SCREEN_WIDTH);
            int y = Math.floorMod(i * 59 + passed * (i % 3 + 3) * 5, 600);
            boolean crystal = i % 4 == 0;
            g.setColor(crystal ? new Color(145, 218, 255, 210) : new Color(248, 253, 255, 220));
            int size = crystal ? 10 : 5;
            g.fillRect(x, y, size, size);
            if (crystal) {
                g.drawLine(x - 4, y + 5, x + 14, y + 5);
                g.drawLine(x + 5, y - 4, x + 5, y + 14);
            }
        }
    }

    private static void drawCastle(Graphics2D g, int passed) {
        int phase = passed / 25;
        boolean nightPhase = phase == 1;
        if (nightPhase) {
            g.setColor(new Color(15, 16, 52, 185));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            g.setColor(new Color(214, 225, 255, 235));
            g.fillOval(1016, 38, 88, 88);
        } else {
            g.setColor(new Color(255, 212, 105, 135));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            g.setColor(new Color(255, 238, 160, 235));
            g.fillOval(1010, 32, 100, 100);
        }
    }
}
