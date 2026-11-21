package kr.ac.jbnu.se.tetris.art;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import kr.ac.jbnu.se.tetris.Art.Pix;
import static kr.ac.jbnu.se.tetris.Art.*;
public final class DesertArt implements StageArt {
    public BufferedImage background(int stage) {
        Pix canvas = new Pix(427, 240);
        desertBg(canvas, new Random(stage * 101L));
        return canvas.toImage(3);
    }
    public BufferedImage monster(int stage) {
        Pix canvas = new Pix(76, 100);
        desertMonster(canvas, new Random(stage * 101L + 1));
        canvas.outline(0x1f2a14);
        return canvas.toImage(3);
    }
    static void desertBg(Pix c, Random rnd) {
        bands(c, new int[]{0xc9563f, 0xd96a45, 0xe8824c, 0xf29d56, 0xf7b866, 0xfbd27e}, 0, 160);
        c.ellipse(300, 96, 27, 27, 0xffd36b);
        c.ellipse(300, 96, 22, 22, 0xfff1b0);
        // 멀리 보이는 탁자 모양 바위산
        mesa(c, 18, 112, 70, 0xb8603f);
        mesa(c, 336, 118, 64, 0xb05a3b);
        mesa(c, 150, 128, 40, 0xc26a45);
        ridge(c, x -> 150 + (int) (6 * Math.sin(x * 0.03) + 4 * Math.sin(x * 0.071 + 1)), 0xe2a35b, 0xf0bc72);
        cactus(c, 62, 150, 0x4f7a36);
        cactus(c, 395, 146, 0x4a7233);
        ridge(c, x -> 176 + (int) (8 * Math.sin(x * 0.022 + 2) + 5 * Math.sin(x * 0.05)), 0xd68f47, 0xe6a45a);
        cactus(c, 238, 178, 0x456e30);
        ridge(c, x -> 206 + (int) (6 * Math.sin(x * 0.018 + 4) + 3 * Math.sin(x * 0.09)), 0xc27a3a, 0xd48b46);
        // 모래 물결 무늬와 자갈
        for (int i = 0; i < 160; i++) {
            int x = rnd.nextInt(c.width());
            int y = 180 + rnd.nextInt(60);
            if (c.get(x, y) == 0xd68f47 || c.get(x, y) == 0xc27a3a) {
                c.rect(x, y, 3 + rnd.nextInt(4), 1, Pix.scale(c.get(x, y), 0.86));
            }
        }
        for (int i = 0; i < 30; i++) {
            c.rect(rnd.nextInt(c.width()), 214 + rnd.nextInt(26), 2, 1, 0x8a5530);
        }
    }

    static void mesa(Pix c, int x, int top, int w, int col) {
        for (int i = 0; i < w; i++) {
            int slope = Math.min(i, w - 1 - i);
            int y = top + Math.max(0, 8 - slope);
            c.rect(x + i, y, 1, 170 - y, col);
        }
        c.rect(x + 8, top, w - 16, 1, Pix.scale(col, 1.2));
    }

    static void cactus(Pix c, int x, int baseY, int col) {
        c.rect(x, baseY - 14, 3, 14, col);
        c.rect(x - 4, baseY - 9, 4, 2, col);
        c.rect(x - 4, baseY - 13, 2, 5, col);
        c.rect(x + 3, baseY - 7, 3, 2, col);
        c.rect(x + 4, baseY - 11, 2, 5, col);
    }

    static void desertMonster(Pix c, Random rnd) {
        int body = 0x5a9a3c;
        int rib = 0x437a2d;
        int light = 0x7cbf55;
        // 모래 둔덕
        c.ellipse(38, 94, 32, 6, 0xc27a3a);
        c.ellipse(38, 92, 26, 3, 0xd48b46);
        // 양팔(왼팔은 위로, 오른팔은 옆으로 들어 올린 위협 자세)
        c.rect(10, 50, 14, 9, body);
        c.rect(9, 28, 9, 30, body);
        c.ellipse(13.5, 29, 4.5, 4, body);
        c.rect(52, 44, 13, 9, body);
        c.rect(58, 22, 9, 30, body);
        c.ellipse(62.5, 23, 4.5, 4, body);
        // 몸통
        c.rect(23, 22, 30, 70, body);
        c.ellipse(38, 24, 15, 13, body);
        drawBodyShading(c, body, rib, light);
        // 가시
        for (int i = 0; i < 70; i++) {
            int x = 8 + rnd.nextInt(62);
            int y = 14 + rnd.nextInt(78);
            if (c.get(x, y) == rib) {
                c.set(x, y, 0xefe7b8);
            }
        }
        // 화난 얼굴
        c.ellipse(32, 42, 4.5, 3.5, 0xfff4e0);
        c.ellipse(45, 42, 4.5, 3.5, 0xfff4e0);
        c.rect(33, 42, 2, 2, 0x140a06);
        c.rect(44, 42, 2, 2, 0x140a06);
        c.line(26, 34, 35, 38, 2, 0x1f2a14);
        c.line(51, 34, 42, 38, 2, 0x1f2a14);
        c.rect(29, 53, 20, 9, 0x3a1414);
        c.rect(30, 58, 18, 3, 0x8a2a2a);
        for (int x = 30; x < 48; x += 3) {
            c.rect(x, 53, 2, 2, 0xfff4e0);
            c.rect(x + 1, 60, 2, 2, 0xfff4e0);
        }
        // 머리 위 꽃
        c.ellipse(44, 10, 3, 3, 0xe65a8a);
        c.ellipse(50, 10, 3, 3, 0xe65a8a);
        c.ellipse(47, 6, 3, 3, 0xf07aa2);
        c.ellipse(47, 13, 3, 3, 0xd04a78);
        c.ellipse(47, 10, 2, 2, 0xffd35a);
    }
    private static void drawBodyShading(Pix c, int body, int rib, int light) {
        for (int y = 14; y < 92; y++) {
            for (int x = 0; x < c.width(); x++) {
                if (c.get(x, y) != body) {
                    continue;
                }
                if (x == 30 || x == 38 || x == 46 || x == 13 || x == 62) {
                    c.set(x, y, rib);
                } else if (x == 25 || x == 26 || x == 11) {
                    c.set(x, y, light);
                } else if (x > 48 && x < 54) {
                    c.set(x, y, Pix.scale(body, 0.85));
                }
            }
        }
    }

}
