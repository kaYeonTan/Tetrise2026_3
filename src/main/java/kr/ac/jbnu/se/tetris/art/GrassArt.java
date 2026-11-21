package kr.ac.jbnu.se.tetris.art;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import kr.ac.jbnu.se.tetris.Art.Pix;
import static kr.ac.jbnu.se.tetris.Art.*;
public final class GrassArt implements StageArt {
    public BufferedImage background(int stage) {
        Pix canvas = new Pix(427, 240);
        grassBg(canvas, new Random(stage * 101L));
        return canvas.toImage(3);
    }
    public BufferedImage monster(int stage) {
        Pix canvas = new Pix(112, 118);
        grassMonster(canvas, new Random(stage * 101L + 1));
        canvas.outline(0x1a2a12);
        return canvas.toImage(3);
    }
    public void effect(Graphics2D g, int width, int height, long time) {  }
    static void grassBg(Pix c, Random rnd) {
        bands(c, new int[]{0x4aa3e0, 0x5fb2e8, 0x77c0ec, 0x90cdef, 0xa9daf2, 0xc2e6f5}, 0, 150);
        cloud(c, 62, 32, 16);
        cloud(c, 214, 50, 12);
        cloud(c, 350, 26, 18);
        ridge(c, x -> 118 + (int) (14 * Math.abs(Math.sin(x * 0.018)) + 6 * Math.sin(x * 0.05)), 0x9cbfd0, -1);
        ridge(c, x -> 142 + (int) (6 * Math.sin(x * 0.025 + 1) + 3 * Math.sin(x * 0.07)), 0x7fb35a, 0x94c66c);
        for (int i = 0; i < 12; i++) {
            int x = rnd.nextInt(c.width());
            int base = 142 + (int) (6 * Math.sin(x * 0.025 + 1) + 3 * Math.sin(x * 0.07));
            c.rect(x, base - 5, 1, 5, 0x4d3a26);
            c.ellipse(x + 0.5, base - 8, 4, 4, 0x3f7a33);
            c.ellipse(x - 0.5, base - 9, 2, 2, 0x5a9a44);
        }
        ridge(c, x -> 168 + (int) (7 * Math.sin(x * 0.02 + 3) + 4 * Math.sin(x * 0.06)), 0x5f9a3f, 0x74b04e);
        ridge(c, x -> 200 + (int) (5 * Math.sin(x * 0.017 + 5) + 3 * Math.sin(x * 0.08)), 0x4a8233, 0x5f9a3f);
        // 풀 무더기와 꽃
        for (int i = 0; i < 220; i++) {
            int x = rnd.nextInt(c.width());
            int y = 170 + rnd.nextInt(70);
            int base = c.get(x, y);
            if (base == 0x5f9a3f || base == 0x4a8233) {
                c.set(x, y, Pix.scale(base, 0.8));
                c.set(x + 1, y - 1, Pix.scale(base, 0.8));
                c.set(x + 2, y, Pix.scale(base, 0.8));
            }
        }
        int[] flowers = {0xe25a4a, 0xf2d14a, 0xffffff, 0xd97ad8};
        for (int i = 0; i < 70; i++) {
            int x = rnd.nextInt(c.width());
            int y = 176 + rnd.nextInt(64);
            c.set(x, y, flowers[rnd.nextInt(flowers.length)]);
        }
    }

    static void grassMonster(Pix c, Random rnd) {
        int bark = 0x7a5535;
        int barkDark = 0x4d331e;
        int barkLight = 0x946a44;
        // 뿌리
        c.line(40, 104, 20, 116, 4, bark);
        c.line(50, 106, 42, 117, 4, bark);
        c.line(64, 106, 72, 117, 4, bark);
        c.line(72, 104, 94, 116, 4, bark);
        // 가지 팔
        c.line(42, 62, 12, 42, 6, bark);
        c.line(22, 48, 14, 30, 3, bark);
        c.line(70, 60, 100, 38, 6, bark);
        c.line(90, 44, 100, 26, 3, bark);
        // 줄기(아래로 갈수록 굵다)
        drawTrunk(c, bark, barkDark, barkLight);
        // 얼굴: 움푹 파인 눈 속 노란 빛, 벌어진 입
        c.ellipse(47, 72, 7, 6, 0x2a1a0e);
        c.ellipse(66, 72, 7, 6, 0x2a1a0e);
        c.ellipse(47, 73, 2.6, 2.2, 0xffe066);
        c.ellipse(66, 73, 2.6, 2.2, 0xffe066);
        c.line(38, 64, 53, 67, 2, barkDark);
        c.line(75, 64, 60, 67, 2, barkDark);
        c.ellipse(56.5, 92, 10, 7, 0x2a1a0e);
        for (int x = 49; x < 64; x += 3) {
            c.rect(x, 86, 2, 2, barkLight);
        }
        // 잎 덩어리
        int leafDark = 0x2f6b2f;
        int leaf = 0x3f8a3a;
        int leafLight = 0x5aa84a;
        c.ellipse(56, 30, 44, 22, leafDark);
        c.ellipse(30, 30, 20, 16, leafDark);
        c.ellipse(84, 30, 20, 16, leafDark);
        c.ellipse(56, 18, 30, 14, leaf);
        c.ellipse(34, 26, 14, 10, leaf);
        c.ellipse(80, 24, 14, 10, leaf);
        c.ellipse(46, 12, 12, 6, leafLight);
        c.ellipse(74, 16, 8, 5, leafLight);
        c.ellipse(28, 22, 6, 4, leafLight);
        c.ellipse(12, 40, 8, 6, leaf);
        c.ellipse(100, 36, 8, 6, leaf);
        c.ellipse(15, 28, 6, 5, leaf);
        c.ellipse(100, 24, 6, 5, leaf);
        for (int i = 0; i < 90; i++) {
            int x = rnd.nextInt(c.width());
            int y = rnd.nextInt(56);
            if (c.get(x, y) == leafDark || c.get(x, y) == leaf) {
                c.set(x, y, Pix.scale(c.get(x, y), 1.25));
            }
        }
        // 이끼와 버섯
        c.rect(40, 100, 6, 2, 0x5a9a3c);
        c.ellipse(72, 101, 3, 2, 0xd9483a);
        c.set(71, 100, 0xffffff);
    }
    private static void drawTrunk(Pix c, int bark, int barkDark, int barkLight) {
        for (int y = 48; y < 110; y++) {
            int half = 17 + (y - 48) / 8;
            for (int x = 56 - half; x <= 56 + half; x++) {
                int col = bark;
                if (x < 56 - half + 4) {
                    col = barkLight;
                } else if (x > 56 + half - 5) {
                    col = Pix.scale(bark, 0.8);
                }
                c.set(x, y, col);
            }
        }
        for (int i = 0; i < 7; i++) {
            int x = 42 + i * 4;
            for (int y = 50; y < 108; y++) {
                if ((y + i * 3) % 11 < 6) {
                    c.set(x + (int) Math.round(Math.sin(y * 0.3 + i)), y, barkDark);
                }
            }
        }
    }

}
