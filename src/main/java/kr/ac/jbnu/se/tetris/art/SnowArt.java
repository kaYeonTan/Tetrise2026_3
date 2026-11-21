package kr.ac.jbnu.se.tetris.art;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import kr.ac.jbnu.se.tetris.Art.Pix;
import static kr.ac.jbnu.se.tetris.Art.*;
public final class SnowArt implements StageArt {
    public BufferedImage background(int stage) {
        Pix canvas = new Pix(427, 240);
        snowBg(canvas, new Random(stage * 101L));
        return canvas.toImage(3);
    }
    public BufferedImage monster(int stage) {
        Pix canvas = new Pix(104, 108);
        snowMonster(canvas, new Random(stage * 101L + 1));
        canvas.outline(0x2e3a4c);
        return canvas.toImage(3);
    }
    public void effect(Graphics2D g, int width, int height, long time) { snowEffect(g, width, height, time); }
    static void snowBg(Pix c, Random rnd) {
        bands(c, new int[]{0x7fa6c7, 0x8fb5d3, 0x9fc2dc, 0xb1cfe4, 0xc3dbeb, 0xd5e7f1}, 0, 150);
        // 먼 산맥
        ridge(c, x -> 104 + (int) (22 * Math.abs(Math.sin(x * 0.021 + 0.4)) + 6 * Math.sin(x * 0.09)), 0xc8d9e6, 0xe4eef5);
        // 가까운 설산: 봉우리 왼쪽은 햇빛, 오른쪽은 그늘
        int[][] peaks = {{60, 70}, {170, 58}, {300, 66}, {400, 80}};
        drawSnowPeaks(c, peaks);
        ridge(c, x -> 160 + (int) (5 * Math.sin(x * 0.03) + 3 * Math.sin(x * 0.08)), 0xe6eff6, 0xffffff);
        ridge(c, x -> 188 + (int) (6 * Math.sin(x * 0.02 + 2)), 0xf2f7fb, 0xffffff);
        // 눈 그늘 물결
        for (int i = 0; i < 90; i++) {
            int x = rnd.nextInt(c.width());
            int y = 192 + rnd.nextInt(48);
            c.rect(x, y, 5 + rnd.nextInt(6), 1, 0xd4e3ef);
        }
        // 눈 덮인 전나무
        int[] treesX = {24, 110, 138, 250, 372, 410};
        for (int i = 0; i < treesX.length; i++) {
            pine(c, treesX[i], 176 + (i % 3) * 6, 10 + (i % 2) * 4);
        }
        for (int i = 0; i < 140; i++) {
            c.set(rnd.nextInt(c.width()), rnd.nextInt(170), 0xffffff);
        }
    }

    private static void drawSnowPeaks(Pix c, int[][] peaks) {
        for (int[] p : peaks) {
            for (int x = p[0] - 70; x <= p[0] + 70; x++) {
                int top = p[1] + (int) (Math.abs(x - p[0]) * 0.95) + (x % 5 == 0 ? 1 : 0);
                for (int y = top; y < 170; y++) {
                    boolean shadow = x > p[0] && y < top + 60;
                    c.set(x, y, shadow ? 0xaac3d8 : 0xeef5fa);
                }
                c.set(x, top, 0xffffff);
            }
        }
    }

    static void pine(Pix c, int x, int baseY, int size) {
        c.rect(x - 1, baseY - 3, 3, 4, 0x3b2a1e);
        for (int i = 0; i < 3; i++) {
            int top = baseY - 4 - i * (size / 2);
            int half = size - i * (size / 3);
            for (int dy = 0; dy < size / 2 + 2; dy++) {
                int w = half * dy / (size / 2 + 2);
                c.rect(x - w, top - (size / 2 + 2) + dy, w * 2 + 1, 1, 0x2f4a3f);
            }
            c.rect(x - half / 3, top - size / 2 - 1, half / 3 * 2 + 1, 1, 0xffffff);
        }
    }

    static void snowMonster(Pix c, Random rnd) {
        int fur = 0xf4f8fb;
        int shade = 0xc7d6e3;
        int face = 0x8fa6bd;
        // 다리
        c.ellipse(36, 98, 10, 8, fur);
        c.ellipse(68, 98, 10, 8, fur);
        // 긴 팔과 발톱
        c.ellipse(16, 70, 10, 24, fur);
        c.ellipse(88, 70, 10, 24, fur);
        for (int i = 0; i < 3; i++) {
            c.line(10 + i * 5, 92, 9 + i * 5, 99, 2, 0x5a6a7a);
            c.line(82 + i * 5, 92, 83 + i * 5, 99, 2, 0x5a6a7a);
        }
        // 몸통
        c.ellipse(52, 66, 32, 32, fur);
        // 뿔
        c.line(36, 22, 28, 12, 4, 0x8a7a6a);
        c.line(28, 12, 30, 4, 3, 0x8a7a6a);
        c.line(68, 22, 76, 12, 4, 0x8a7a6a);
        c.line(76, 12, 74, 4, 3, 0x8a7a6a);
        // 머리
        c.ellipse(52, 32, 21, 18, fur);
        // 털 결: 가장자리를 들쭉날쭉하게, 오른쪽 아래는 그늘
        drawFurTexture(c, rnd, fur, shade);
        drawSnowFace(c, face);

        c.line(52, 58, 52, 74, 1, 0x9fd6f2);
        c.line(44, 66, 60, 66, 1, 0x9fd6f2);
        c.line(46, 60, 58, 72, 1, 0x9fd6f2);
        c.line(58, 60, 46, 72, 1, 0x9fd6f2);
    }

    private static void drawFurTexture(Pix c, Random rnd, int fur, int shade) {
        for (int y = 0; y < c.height(); y++) {
            for (int x = 0; x < c.width(); x++) {
                if (c.get(x, y) != fur) {
                    continue;
                }
                double dx = x - 52;
                double dy = y - 66;
                if (dx * 0.6 + dy * 0.8 > 16 || (x > 90 && y > 50) || (x > 22 && x < 26 && y > 50)) {
                    c.set(x, y, shade);
                }
            }
        }
        addFurEdges(c, rnd, fur);
        addFurTufts(c, rnd, fur, shade);
    }

    private static void addFurEdges(Pix c, Random rnd, int fur) {
        for (int i = 0; i < 60; i++) {
            int x = rnd.nextInt(c.width());
            int y = rnd.nextInt(c.height());
            if (c.get(x, y) == Pix.CLEAR && (c.get(x + 1, y) == fur || c.get(x - 1, y) == fur)) {
                c.set(x, y, fur);
            }
        }
    }

    private static void addFurTufts(Pix c, Random rnd, int fur, int shade) {
        // 털 뭉치 결
        for (int i = 0; i < 40; i++) {
            int x = 24 + rnd.nextInt(56);
            int y = 40 + rnd.nextInt(50);
            if (c.get(x, y) == fur && c.get(x + 1, y + 1) == fur) {
                c.set(x, y, shade);
                c.set(x + 1, y + 1, shade);
            }
        }
        // 얼굴
    }

    private static void drawSnowFace(Pix c, int face) {
        c.ellipse(52, 36, 13, 11, face);
        c.ellipse(46, 32, 3.5, 3, 0xff4a4a);
        c.ellipse(58, 32, 3.5, 3, 0xff4a4a);
        c.rect(45, 31, 2, 2, 0xffd0d0);
        c.rect(57, 31, 2, 2, 0xffd0d0);
        c.line(40, 26, 50, 29, 2, 0x3a4a5c);
        c.line(64, 26, 54, 29, 2, 0x3a4a5c);
        c.ellipse(52, 42, 8, 4.5, 0x3a1d2a);
        c.rect(45, 38, 3, 4, 0xffffff);
        c.rect(57, 38, 3, 4, 0xffffff);
        // 배의 얼음 결정
    }

    static void snowEffect(Graphics2D g, int width, int height, long timeMs) {
        g.setColor(new Color(255, 255, 255, 210));
        for (int i = 0; i < 70; i++) {
            long speed = 40 + (i * 37) % 50;
            int x = (int) ((i * 173 + Math.sin((timeMs / 900.0) + i) * 12) % width);
            int y = (int) (((i * 97) + timeMs * speed / 1000) % height);
            int s = i % 3 == 0 ? 6 : 3;
            g.fillRect(Math.floorMod(x, width), y, s, s);
        }
    }
}
