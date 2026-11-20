package kr.ac.jbnu.se.tetris.art;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import kr.ac.jbnu.se.tetris.Art.Pix;
import static kr.ac.jbnu.se.tetris.Art.*;
public final class IslandArt implements StageArt {
    public BufferedImage background(int stage) {
        Pix canvas = new Pix(427, 240);
        islandBg(canvas, new Random(stage * 101L));
        return canvas.toImage(3);
    }
    public BufferedImage monster(int stage) {
        Pix canvas = new Pix(100, 112);
        islandMonster(canvas, new Random(stage * 101L + 1));
        canvas.outline(0x0b2230);
        return canvas.toImage(3);
    }
    public void effect(Graphics2D g, int width, int height, long time) { islandEffect(g, width, height, time); }
    static void islandBg(Pix c, Random rnd) {
        bands(c, new int[]{0x141c28, 0x1b2735, 0x22324a, 0x2b3f5c, 0x35506e, 0x41627f}, 0, 132);
        // 먹구름
        for (int i = 0; i < 9; i++) {
            int cx = rnd.nextInt(c.width());
            int cy = 8 + rnd.nextInt(40);
            c.ellipse(cx, cy, 30 + rnd.nextInt(30), 7 + rnd.nextInt(5), i % 2 == 0 ? 0x0f1620 : 0x18222f);
        }
        // 번개
        int[][] bolt = {{96, 0}, {88, 30}, {98, 34}, {80, 70}, {92, 72}, {72, 118}};
        for (int i = 0; i + 1 < bolt.length; i++) {
            c.line(bolt[i][0], bolt[i][1], bolt[i + 1][0], bolt[i + 1][1], 3, 0x9fc4e8);
        }
        for (int i = 0; i + 1 < bolt.length; i++) {
            c.line(bolt[i][0], bolt[i][1], bolt[i + 1][0], bolt[i + 1][1], 1, 0xf2f8ff);
        }
        // 바다
        bands(c, new int[]{0x2a5a78, 0x1f4a66, 0x173d57, 0x113148, 0x0c263a}, 132, 240);
        // 멀리 있는 외딴섬과 죽은 야자수
        c.ellipse(70, 134, 46, 9, 0x2c2b31);
        c.ellipse(64, 131, 30, 6, 0x3b3a3f);
        c.line(72, 128, 76, 104, 2, 0x1f1d22);
        c.line(76, 104, 62, 100, 1, 0x1f1d22);
        c.line(76, 104, 90, 98, 1, 0x1f1d22);
        c.line(76, 104, 70, 112, 1, 0x1f1d22);
        c.line(76, 104, 86, 110, 1, 0x1f1d22);
        // 부서진 신전 기둥
        c.rect(360, 112, 6, 22, 0x2f3440);
        c.rect(376, 118, 6, 16, 0x2f3440);
        c.rect(356, 110, 30, 3, 0x2f3440);
        c.ellipse(371, 134, 22, 4, 0x24272f);
        // 파도 하이라이트: 가까울수록 크고 많게
        for (int i = 0; i < 260; i++) {
            int y = 134 + rnd.nextInt(106);
            int x = rnd.nextInt(c.width());
            int len = 2 + (y - 130) / 12 + rnd.nextInt(3);
            c.rect(x, y, len, 1, y > 200 ? 0x4a86aa : 0x3f769a);
        }
        // 비
        for (int i = 0; i < 180; i++) {
            int x = rnd.nextInt(c.width());
            int y = rnd.nextInt(c.height());
            c.set(x, y, 0x6f8fb0);
            c.set(x - 1, y + 1, 0x6f8fb0);
            c.set(x - 2, y + 2, 0x56769a);
        }
    }

    static void islandMonster(Pix c, Random rnd) {
        int skin = 0x4fb3c4;
        int dark = 0x2f8a9e;
        int light = 0x8fe0e8;
        int weed = 0x2f7f6f;
        int gold = 0xe3c14b;
        // 삼지창
        c.rect(13, 10, 3, 92, gold);
        c.rect(6, 20, 17, 3, gold);
        c.rect(6, 6, 3, 16, gold);
        c.rect(13, 2, 3, 10, gold);
        c.rect(20, 6, 3, 16, gold);
        c.set(7, 5, 0xfff1a8);
        c.set(14, 1, 0xfff1a8);
        c.set(21, 5, 0xfff1a8);
        // 흘러내리는 머리카락(해초)
        for (int i = 0; i < 6; i++) {
            c.line(40 - i * 3, 26 + i * 2, 26 - i * 2, 60 + i * 4, 2, weed);
            c.line(60 + i * 3, 26 + i * 2, 76 + i * 2, 60 + i * 4, 2, weed);
        }
        // 몸통: 허리에서 물속으로 흩어진다
        drawIslandBody(c, dark, light, skin);
        c.line(40, 66, 50, 70, 1, dark);
        c.line(60, 66, 50, 70, 1, dark);
        c.line(50, 72, 50, 86, 1, dark);
        // 팔: 왼팔은 삼지창을, 오른팔은 갈퀴손을 뻗는다
        c.line(28, 54, 16, 60, 6, skin);
        c.ellipse(15, 60, 5, 5, skin);
        c.line(72, 54, 86, 74, 6, skin);
        c.line(86, 74, 92, 84, 2, light);
        c.line(86, 74, 88, 86, 2, light);
        c.line(86, 74, 83, 86, 2, light);
        // 머리
        c.ellipse(50, 36, 16, 17, skin);
        c.ellipse(45, 30, 5, 5, light);
        c.ellipse(58, 40, 6, 10, dark);
        // 왕관
        c.rect(36, 16, 28, 5, gold);
        for (int i = 0; i < 4; i++) {
            c.line(38 + i * 8, 16, 40 + i * 8, 8, 2, gold);
        }
        c.rect(48, 17, 3, 3, 0xd94a4a);
        // 빛나는 눈과 입
        c.ellipse(44, 35, 5, 3.5, 0x0f3a48);
        c.ellipse(57, 35, 5, 3.5, 0x0f3a48);
        c.rect(42, 34, 4, 2, 0xe8fff8);
        c.rect(55, 34, 4, 2, 0xe8fff8);
        c.rect(46, 45, 9, 3, 0x0f3a48);
        // 수염
        for (int i = 0; i < 5; i++) {
            c.line(42 + i * 4, 47, 40 + i * 5, 62 + (i % 2) * 5, 2, weed);
        }
        // 물보라 받침
        c.ellipse(50, 104, 46, 8, 0x2f7fa0);
        c.ellipse(50, 102, 40, 5, 0x3f96b8);
        for (int x = 8; x < 92; x += 4) {
            c.rect(x, 97 + (x % 8 == 0 ? 0 : 2), 3, 2, 0xcfe6f2);
        }
    }

    private static void drawIslandBody(Pix c, int dark, int light, int skin) {
        for (int y = 48; y < 100; y++) {
            double t = (y - 48) / 52.0;
            int half = (int) (25 - t * 11);
            for (int x = 50 - half; x <= 50 + half; x++) {
                int col = x > 50 + half - 6 ? dark : (x < 50 - half + 4 ? light : skin);
                if (y > 88 && (x + y) % 2 == 0) {
                    col = 0x2f7fa0;
                }
                c.set(x, y, col);
            }
        }
    }

    static void islandEffect(Graphics2D g, int width, int height, long timeMs) {
        long t = timeMs % 7000;
        if (t < 90 || (t > 180 && t < 240)) {
            g.setColor(new Color(220, 235, 255, 60));
            g.fillRect(0, 0, width, height);
        }
    }
}
