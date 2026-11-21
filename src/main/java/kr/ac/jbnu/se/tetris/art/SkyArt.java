package kr.ac.jbnu.se.tetris.art;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import kr.ac.jbnu.se.tetris.Art.Pix;
import static kr.ac.jbnu.se.tetris.Art.*;
public final class SkyArt implements StageArt {
    public BufferedImage background(int stage) {
        Pix canvas = new Pix(427, 240);
        skyBg(canvas, new Random(stage * 101L));
        return canvas.toImage(3);
    }
    public BufferedImage monster(int stage) {
        Pix canvas = new Pix(118, 112);
        skyMonster(canvas, new Random(stage * 101L + 1));
        canvas.outline(0x0a0510);
        return canvas.toImage(3);
    }
    public void effect(Graphics2D g, int width, int height, long time) { skyEffect(g, width, height, time); }
    static void skyBg(Pix c, Random rnd) {
        bands(c, new int[]{0x140a1f, 0x1c0d2a, 0x261135, 0x321540, 0x40194a, 0x521c50, 0x681f52, 0x7e2550}, 0, 240);
        for (int i = 0; i < 90; i++) {
            c.set(rnd.nextInt(c.width()), rnd.nextInt(130), i % 4 == 0 ? 0xe8d0f0 : 0x9a7aac);
        }
        // 핏빛 달
        c.ellipse(320, 62, 36, 36, 0x9e3029);
        c.ellipse(318, 60, 34, 34, 0xc9453a);
        c.ellipse(310, 50, 16, 14, 0xe0634e);
        c.ellipse(334, 72, 6, 5, 0x9e3029);
        c.ellipse(304, 78, 4, 3, 0x9e3029);
        c.ellipse(340, 48, 3, 3, 0xa8362d);
        // 떠 있는 폐허와 마왕성 실루엣
        floatingRock(c, 52, 96, 28);
        c.rect(40, 70, 8, 26, 0x1a0f24);
        c.rect(52, 62, 10, 34, 0x1a0f24);
        c.rect(66, 78, 7, 18, 0x1a0f24);
        c.rect(54, 56, 2, 6, 0x1a0f24);
        c.rect(58, 56, 2, 6, 0x1a0f24);
        c.rect(55, 70, 2, 3, 0xff6a3a);
        floatingRock(c, 392, 150, 20);
        c.rect(386, 130, 4, 20, 0x1a0f24);
        c.rect(396, 136, 4, 14, 0x1a0f24);
        floatingRock(c, 190, 118, 10);
        // 구름 바다
        for (int x = 0; x < c.width(); x += 14) {
            int top = 188 + (int) (7 * Math.sin(x * 0.05) + 4 * Math.sin(x * 0.13));
            c.ellipse(x, top + 8, 16, 10, 0x3a2250);
            c.ellipse(x + 4, top + 5, 10, 6, 0x5a3470);
        }
        c.rect(0, 206, c.width(), 34, 0x3a2250);
        for (int x = 0; x < c.width(); x += 9) {
            c.rect(x, 214 + (x % 18 == 0 ? 0 : 6), 5, 1, 0x4a2c62);
        }
    }

    static void floatingRock(Pix c, int cx, int top, int w) {
        for (int y = 0; y < w; y++) {
            int half = (w - y) * 2 / 2 + 2;
            c.rect(cx - half, top + y, half * 2, 1, y < 2 ? 0x4a3050 : 0x2a1a33);
        }
    }

    static void skyMonster(Pix c, Random rnd) {
        int wing = 0x3a1238;
        int bone = 0x1c0a1e;
        int skin = 0x3a1f3f;
        int skinLight = 0x553060;
        // 박쥐 날개
        wing(c, 44, -1, wing, bone);
        wing(c, 74, 1, wing, bone);
        // 불꽃 아우라(하반신)
        for (int i = 0; i < 12; i++) {
            int x = 34 + i * 4 + rnd.nextInt(3);
            int h = 10 + rnd.nextInt(14);
            c.rect(x, 100 - h, 4, h, 0xff5a2a);
            c.rect(x + 1, 104 - h / 2, 2, h / 2, 0xffb23a);
        }
        c.ellipse(59, 102, 28, 8, 0xd9402a);
        // 몸통
        for (int y = 46; y < 96; y++) {
            int half = 18 - Math.max(0, (y - 70) / 4);
            c.rect(59 - half, y, half * 2, 1, skin);
            c.rect(59 - half, y, 3, 1, skinLight);
        }
        c.line(52, 58, 52, 84, 1, 0x4d2a57);
        c.line(66, 58, 66, 84, 1, 0x4d2a57);
        c.line(48, 66, 70, 66, 1, 0x4d2a57);
        c.line(48, 76, 70, 76, 1, 0x4d2a57);
        c.ellipse(59, 52, 6, 4, 0x9a2a3a);
        // 팔: 오른손에 보라색 구슬
        c.line(42, 50, 26, 80, 7, skin);
        for (int i = 0; i < 3; i++) {
            c.line(24 + i * 3, 82, 22 + i * 3, 89, 2, 0xe8dcc0);
        }
        c.line(76, 50, 94, 74, 7, skin);
        c.ellipse(98, 78, 9, 9, 0x6a2ab8);
        c.ellipse(96, 76, 5, 5, 0x9a4ad8);
        c.rect(94, 73, 2, 2, 0xe8c8ff);
        // 머리와 뿔
        c.line(50, 22, 38, 10, 5, 0xe8dcc0);
        c.line(38, 10, 34, 0, 3, 0xe8dcc0);
        c.line(68, 22, 80, 10, 5, 0xe8dcc0);
        c.line(80, 10, 84, 0, 3, 0xe8dcc0);
        c.ellipse(59, 32, 14, 15, skin);
        c.ellipse(55, 26, 5, 5, skinLight);
        // 이글거리는 눈과 찢어진 웃음
        c.line(49, 29, 55, 32, 3, 0xff3a3a);
        c.line(69, 29, 63, 32, 3, 0xff3a3a);
        c.rect(52, 31, 2, 1, 0xffd24a);
        c.rect(64, 31, 2, 1, 0xffd24a);
        c.line(50, 39, 59, 43, 2, 0x1a0510);
        c.line(68, 39, 59, 43, 2, 0x1a0510);
        for (int x = 52; x < 67; x += 3) {
            c.set(x, 40 + Math.abs(59 - x) / 4 * -1 + 1, 0xfff4e6);
        }
    }

    static void wing(Pix c, int sx, int dir, int membrane, int bone) {
        int[][] tips = {{sx + dir * 42, WING_ROOT_Y - 26}, {sx + dir * 46, WING_ROOT_Y + 6}, {sx + dir * 36, WING_ROOT_Y + 30}, {sx + dir * 20, WING_ROOT_Y + 40}};
        for (int i = 0; i + 1 < tips.length; i++) {
            fillTriangle(c, sx, WING_ROOT_Y, tips[i][0], tips[i][1], tips[i + 1][0], tips[i + 1][1], membrane);
        }
        fillTriangle(c, sx, WING_ROOT_Y, sx + dir * 30, WING_ROOT_Y - 34, tips[0][0], tips[0][1], membrane);
        for (int[] t : tips) {
            c.line(sx, WING_ROOT_Y, t[0], t[1], 2, bone);
        }
        c.line(sx, WING_ROOT_Y, sx + dir * 30, WING_ROOT_Y - 34, 3, bone);
        c.line(sx + dir * 30, WING_ROOT_Y - 34, tips[0][0], tips[0][1], 2, bone);
    }

    static void fillTriangle(Pix c, int x0, int y0, int x1, int y1, int x2, int y2, int rgb) {
        int minX = Math.min(x0, Math.min(x1, x2));
        int maxX = Math.max(x0, Math.max(x1, x2));
        int minY = Math.min(y0, Math.min(y1, y2));
        int maxY = Math.max(y0, Math.max(y1, y2));
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double d1 = (x - x1) * (y0 - y1) - (x0 - x1) * (y - y1);
                double d2 = (x - x2) * (y1 - y2) - (x1 - x2) * (y - y2);
                double d3 = (x - x0) * (y2 - y0) - (x2 - x0) * (y - y0);
                boolean neg = d1 < 0 || d2 < 0 || d3 < 0;
                boolean pos = d1 > 0 || d2 > 0 || d3 > 0;
                if (!(neg && pos)) {
                    c.set(x, y, rgb);
                }
            }
        }
    }

    static void skyEffect(Graphics2D g, int width, int height, long timeMs) {
        int a = (int) (22 + 18 * Math.sin(timeMs / 700.0));
        g.setColor(new Color(160, 20, 40, Math.max(0, a)));
        g.fillRect(0, height - 160, width, 160);
    }
}
