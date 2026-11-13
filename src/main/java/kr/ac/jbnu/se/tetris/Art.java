package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Art {

    static BufferedImage[] bgImg = new BufferedImage[6];
    static BufferedImage[] monsterImg = new BufferedImage[6];
    static BufferedImage[] flameImg;

    public static BufferedImage background(int stage) {
        if (bgImg[stage] == null) {
            Pix c = new Pix(427, 240);
            Random rnd = new Random(stage * 101);
            switch (stage) {
                case 1:
                    desertBg(c, rnd);
                    break;
                case 2:
                    islandBg(c, rnd);
                    break;
                case 3:
                    grassBg(c, rnd);
                    break;
                case 4:
                    snowBg(c, rnd);
                    break;
                default:
                    skyBg(c, rnd);
                    break;
            }
            bgImg[stage] = c.toImage(3);
        }
        return bgImg[stage];
    }

    public static BufferedImage monster(int stage) {
        if (monsterImg[stage] == null) {
            Pix c;
            Random rnd = new Random(stage * 101 + 1);
            if (stage == 1) {
                c = new Pix(76, 100);
                desertMonster(c, rnd);
                c.outline(0x1f2a14);
            } else if (stage == 2) {
                c = new Pix(100, 112);
                islandMonster(c, rnd);
                c.outline(0x0b2230);
            } else if (stage == 3) {
                c = new Pix(112, 118);
                grassMonster(c, rnd);
                c.outline(0x1a2a12);
            } else if (stage == 4) {
                c = new Pix(104, 108);
                snowMonster(c, rnd);
                c.outline(0x2e3a4c);
            } else {
                c = new Pix(118, 112);
                skyMonster(c, rnd);
                c.outline(0x0a0510);
            }
            monsterImg[stage] = c.toImage(3);
        }
        return monsterImg[stage];
    }

    public static void effect(Graphics2D g, int stage, int w, int h, long time) {
        if (stage == 2) {
            islandEffect(g, w, h, time);
        } else if (stage == 4) {
            snowEffect(g, w, h, time);
        } else if (stage == 5) {
            skyEffect(g, w, h, time);
        }
    }

    public static BufferedImage flame(int frame) {
        if (flameImg == null) {
            String[][] rows = {
                {"...o...", "...o...", "..oo...", "..ooo..", ".oomo..", ".ommmo.",
                 ".ommmo.", "oommmo.", "ommcmmo", "ommcmmo", ".omcmo."},
                {"....o..", "...oo..", "...oo..", "..ooo..", "..omoo.", ".ommmo.",
                 ".ommmoo", ".ommmoo", "ommcmmo", "ommcmmo", ".omcmo."}
            };
            flameImg = new BufferedImage[2];
            for (int i = 0; i < 2; i++) {
                Pix c = new Pix(7, 11);
                for (int y = 0; y < 11; y++) {
                    for (int x = 0; x < 7; x++) {
                        char ch = rows[i][y].charAt(x);
                        if (ch == 'o') c.set(x, y, 0xe8622a);
                        if (ch == 'm') c.set(x, y, 0xf7a531);
                        if (ch == 'c') c.set(x, y, 0xffe9a3);
                    }
                }
                flameImg[i] = c.toImage(3);
            }
        }
        return flameImg[frame % 2];
    }


    static int[][] TORCHES = {{177, 86}, {409, 86}};

    static BufferedImage castleImg;


    static BufferedImage castle() {
        if (castleImg == null) {
            castleImg = paintCastle().toImage(3);
        }
        return castleImg;
    }

    static Pix paintCastle() {
        Pix c = new Pix(427, 240);
        Random rnd = new Random(3);
        int[] stones = {0x27222d, 0x2d2733, 0x322c39, 0x2a2530, 0x302a36};
        int[] floors = {0x1f1b24, 0x241f29, 0x1c1820};
        int brickW = 22;
        int brickH = 11;
        int floorY = 214;
        java.util.Map<Long, Integer> shade = new java.util.HashMap<>();

        for (int y = 0; y < 240; y++) {
            for (int x = 0; x < 427; x++) {
                int col;
                if (y >= floorY) {
                    int fy = y - floorY;
                    int row = fy / 9;
                    int off = (row % 2) * 16;
                    long key = (1L << 40) | ((long) row << 20) | ((x + off) / 32);
                    col = shade.computeIfAbsent(key, k -> floors[rnd.nextInt(floors.length)]);
                    if (fy % 9 == 0 || (x + off) % 32 == 0) {
                        col = 0x0e0c11;
                    }
                    if (y == floorY) {
                        col = 0x4a4252;
                    }
                } else {
                    int row = y / brickH;
                    int off = (row % 2) * (brickW / 2);
                    long key = ((long) row << 20) | ((x + off) / brickW);
                    col = shade.computeIfAbsent(key, k -> stones[rnd.nextInt(stones.length)]);
                    int ly = y % brickH;
                    int lx = (x + off) % brickW;
                    if (ly == 0 || lx == 0) {
                        col = 0x131017;
                    } else if (ly == 1 || lx == 1) {
                        col = Pix.scale(col, 1.18);
                    } else if (ly == brickH - 1 || lx == brickW - 1) {
                        col = Pix.scale(col, 0.82);
                    }
                }
                c.set(x, y, light(col, x, y));
            }
        }
        for (int[] t : TORCHES) {
            drawSconce(c, t[0], t[1]);
        }
        return c;
    }

    static int light(int col, int x, int y) {
        double l = 0;
        for (int[] t : TORCHES) {
            double d = Math.hypot(x - t[0], (y - t[1]) * 1.15);
            l = Math.max(l, 1 - d / 105);
        }
        double q = 0;
        if (l >= 0.78) {
            q = 0.9;
        } else if (l >= 0.55) {
            q = 0.6;
        } else if (l >= 0.32) {
            q = 0.38;
        } else if (l >= 0.12) {
            q = 0.18;
        } else if (l > 0.05 && (x + y) % 2 == 0) {
            q = 0.08;
        }
        int warm = 0xff8a2a;
        int lit = Pix.scale(col, 1 + q * 1.1);
        lit = Pix.mix(lit, warm, q * 0.32);
        double edge = Math.max(Math.abs(x - 427 / 2.0) / (427 / 2.0), Math.abs(y - 240 / 2.0) / (240 / 2.0));
        if (edge > 0.92) {
            lit = Pix.scale(lit, 0.7);
        } else if (edge > 0.84) {
            lit = Pix.scale(lit, 0.85);
        }
        return lit;
    }

    static void drawSconce(Pix c, int tx, int ty) {
        int iron = 0x0f0d12;
        c.rect(tx - 5, ty + 14, 11, 2, iron);
        c.rect(tx - 5, ty + 10, 2, 6, iron);
        c.rect(tx + 4, ty + 10, 2, 6, iron);
        c.rect(tx - 1, ty + 16, 3, 8, iron);
        c.rect(tx - 3, ty + 24, 7, 2, 0x221e27);
        c.rect(tx - 2, ty + 2, 5, 12, 0x5a3a1f);
        c.rect(tx - 2, ty + 2, 1, 12, 0x7a4e28);
        c.rect(tx - 3, ty + 1, 7, 3, 0x2e2116);
    }

    static void bands(Pix c, int[] colors, int top, int bottom) {
        int bandH = Math.max(1, (bottom - top) / colors.length);
        for (int y = top; y < bottom; y++) {
            int i = Math.min(colors.length - 1, (y - top) / bandH);
            boolean edge = (y - top) % bandH == bandH - 1 && i + 1 < colors.length;
            for (int x = 0; x < c.width(); x++) {
                int col = colors[i];
                if (edge && (x + y) % 2 == 0) {
                    col = colors[i + 1];
                }
                c.set(x, y, col);
            }
        }
    }

    static void ridge(Pix c, java.util.function.IntUnaryOperator topAt, int fill, int rim) {
        for (int x = 0; x < c.width(); x++) {
            int top = Math.max(0, topAt.applyAsInt(x));
            for (int y = top; y < c.height(); y++) {
                c.set(x, y, fill);
            }
            if (rim >= 0) {
                c.set(x, top, rim);
            }
        }
    }

    static void cloud(Pix c, int cx, int cy, int size, int light, int shade) {
        c.ellipse(cx, cy + 2, size * 1.6, size * 0.6, shade);
        c.ellipse(cx - size * 0.6, cy, size * 0.7, size * 0.55, light);
        c.ellipse(cx + size * 0.2, cy - size * 0.3, size * 0.8, size * 0.7, light);
        c.ellipse(cx + size * 0.9, cy + 1, size * 0.6, size * 0.45, light);
        c.rect(cx - size, cy + (int) (size * 0.45), size * 2, 1, shade);
    }



    static void desertBg(Pix c, Random rnd) {
        bands(c, new int[]{0xc9563f, 0xd96a45, 0xe8824c, 0xf29d56, 0xf7b866, 0xfbd27e}, 0, 160);
        c.ellipse(300, 96, 27, 27, 0xffd36b);
        c.ellipse(300, 96, 22, 22, 0xfff1b0);
        
        mesa(c, 18, 112, 70, 0xb8603f);
        mesa(c, 336, 118, 64, 0xb05a3b);
        mesa(c, 150, 128, 40, 0xc26a45);
        ridge(c, x -> 150 + (int) (6 * Math.sin(x * 0.03) + 4 * Math.sin(x * 0.071 + 1)), 0xe2a35b, 0xf0bc72);
        cactus(c, 62, 150, 0x4f7a36);
        cactus(c, 395, 146, 0x4a7233);
        ridge(c, x -> 176 + (int) (8 * Math.sin(x * 0.022 + 2) + 5 * Math.sin(x * 0.05)), 0xd68f47, 0xe6a45a);
        cactus(c, 238, 178, 0x456e30);
        ridge(c, x -> 206 + (int) (6 * Math.sin(x * 0.018 + 4) + 3 * Math.sin(x * 0.09)), 0xc27a3a, 0xd48b46);
        
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
        
        c.ellipse(38, 94, 32, 6, 0xc27a3a);
        c.ellipse(38, 92, 26, 3, 0xd48b46);
        
        c.rect(10, 50, 14, 9, body);
        c.rect(9, 28, 9, 30, body);
        c.ellipse(13.5, 29, 4.5, 4, body);
        c.rect(52, 44, 13, 9, body);
        c.rect(58, 22, 9, 30, body);
        c.ellipse(62.5, 23, 4.5, 4, body);
        
        c.rect(23, 22, 30, 70, body);
        c.ellipse(38, 24, 15, 13, body);
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
        
        for (int i = 0; i < 70; i++) {
            int x = 8 + rnd.nextInt(62);
            int y = 14 + rnd.nextInt(78);
            if (c.get(x, y) == rib) {
                c.set(x, y, 0xefe7b8);
            }
        }
        
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
        
        c.ellipse(44, 10, 3, 3, 0xe65a8a);
        c.ellipse(50, 10, 3, 3, 0xe65a8a);
        c.ellipse(47, 6, 3, 3, 0xf07aa2);
        c.ellipse(47, 13, 3, 3, 0xd04a78);
        c.ellipse(47, 10, 2, 2, 0xffd35a);
    }




    static void islandBg(Pix c, Random rnd) {
        bands(c, new int[]{0x141c28, 0x1b2735, 0x22324a, 0x2b3f5c, 0x35506e, 0x41627f}, 0, 132);
        
        for (int i = 0; i < 9; i++) {
            int cx = rnd.nextInt(c.width());
            int cy = 8 + rnd.nextInt(40);
            c.ellipse(cx, cy, 30 + rnd.nextInt(30), 7 + rnd.nextInt(5), i % 2 == 0 ? 0x0f1620 : 0x18222f);
        }
        
        int[][] bolt = {{96, 0}, {88, 30}, {98, 34}, {80, 70}, {92, 72}, {72, 118}};
        for (int i = 0; i + 1 < bolt.length; i++) {
            c.line(bolt[i][0], bolt[i][1], bolt[i + 1][0], bolt[i + 1][1], 3, 0x9fc4e8);
        }
        for (int i = 0; i + 1 < bolt.length; i++) {
            c.line(bolt[i][0], bolt[i][1], bolt[i + 1][0], bolt[i + 1][1], 1, 0xf2f8ff);
        }
        
        bands(c, new int[]{0x2a5a78, 0x1f4a66, 0x173d57, 0x113148, 0x0c263a}, 132, 240);
        
        c.ellipse(70, 134, 46, 9, 0x2c2b31);
        c.ellipse(64, 131, 30, 6, 0x3b3a3f);
        c.line(72, 128, 76, 104, 2, 0x1f1d22);
        c.line(76, 104, 62, 100, 1, 0x1f1d22);
        c.line(76, 104, 90, 98, 1, 0x1f1d22);
        c.line(76, 104, 70, 112, 1, 0x1f1d22);
        c.line(76, 104, 86, 110, 1, 0x1f1d22);
        
        c.rect(360, 112, 6, 22, 0x2f3440);
        c.rect(376, 118, 6, 16, 0x2f3440);
        c.rect(356, 110, 30, 3, 0x2f3440);
        c.ellipse(371, 134, 22, 4, 0x24272f);
        
        for (int i = 0; i < 260; i++) {
            int y = 134 + rnd.nextInt(106);
            int x = rnd.nextInt(c.width());
            int len = 2 + (y - 130) / 12 + rnd.nextInt(3);
            c.rect(x, y, len, 1, y > 200 ? 0x4a86aa : 0x3f769a);
        }
        
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
        
        c.rect(13, 10, 3, 92, gold);
        c.rect(6, 20, 17, 3, gold);
        c.rect(6, 6, 3, 16, gold);
        c.rect(13, 2, 3, 10, gold);
        c.rect(20, 6, 3, 16, gold);
        c.set(7, 5, 0xfff1a8);
        c.set(14, 1, 0xfff1a8);
        c.set(21, 5, 0xfff1a8);
        
        for (int i = 0; i < 6; i++) {
            c.line(40 - i * 3, 26 + i * 2, 26 - i * 2, 60 + i * 4, 2, weed);
            c.line(60 + i * 3, 26 + i * 2, 76 + i * 2, 60 + i * 4, 2, weed);
        }
        
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
        c.line(40, 66, 50, 70, 1, dark);
        c.line(60, 66, 50, 70, 1, dark);
        c.line(50, 72, 50, 86, 1, dark);
        
        c.line(28, 54, 16, 60, 6, skin);
        c.ellipse(15, 60, 5, 5, skin);
        c.line(72, 54, 86, 74, 6, skin);
        c.line(86, 74, 92, 84, 2, light);
        c.line(86, 74, 88, 86, 2, light);
        c.line(86, 74, 83, 86, 2, light);
        
        c.ellipse(50, 36, 16, 17, skin);
        c.ellipse(45, 30, 5, 5, light);
        c.ellipse(58, 40, 6, 10, dark);
        
        c.rect(36, 16, 28, 5, gold);
        for (int i = 0; i < 4; i++) {
            c.line(38 + i * 8, 16, 40 + i * 8, 8, 2, gold);
        }
        c.rect(48, 17, 3, 3, 0xd94a4a);
        
        c.ellipse(44, 35, 5, 3.5, 0x0f3a48);
        c.ellipse(57, 35, 5, 3.5, 0x0f3a48);
        c.rect(42, 34, 4, 2, 0xe8fff8);
        c.rect(55, 34, 4, 2, 0xe8fff8);
        c.rect(46, 45, 9, 3, 0x0f3a48);
        
        for (int i = 0; i < 5; i++) {
            c.line(42 + i * 4, 47, 40 + i * 5, 62 + (i % 2) * 5, 2, weed);
        }
        
        c.ellipse(50, 104, 46, 8, 0x2f7fa0);
        c.ellipse(50, 102, 40, 5, 0x3f96b8);
        for (int x = 8; x < 92; x += 4) {
            c.rect(x, 97 + (x % 8 == 0 ? 0 : 2), 3, 2, 0xcfe6f2);
        }
    }

    static void islandEffect(Graphics2D g, int width, int height, long timeMs) {
        long t = timeMs % 7000;
        if (t < 90 || (t > 180 && t < 240)) {
            g.setColor(new Color(220, 235, 255, 60));
            g.fillRect(0, 0, width, height);
        }
    }




    static void grassBg(Pix c, Random rnd) {
        bands(c, new int[]{0x4aa3e0, 0x5fb2e8, 0x77c0ec, 0x90cdef, 0xa9daf2, 0xc2e6f5}, 0, 150);
        cloud(c, 62, 32, 16, 0xffffff, 0xdcecf6);
        cloud(c, 214, 50, 12, 0xffffff, 0xdcecf6);
        cloud(c, 350, 26, 18, 0xffffff, 0xdcecf6);
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
        
        c.line(40, 104, 20, 116, 4, bark);
        c.line(50, 106, 42, 117, 4, bark);
        c.line(64, 106, 72, 117, 4, bark);
        c.line(72, 104, 94, 116, 4, bark);
        
        c.line(42, 62, 12, 42, 6, bark);
        c.line(22, 48, 14, 30, 3, bark);
        c.line(70, 60, 100, 38, 6, bark);
        c.line(90, 44, 100, 26, 3, bark);
        
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
        
        c.rect(40, 100, 6, 2, 0x5a9a3c);
        c.ellipse(72, 101, 3, 2, 0xd9483a);
        c.set(71, 100, 0xffffff);
    }




    static void snowBg(Pix c, Random rnd) {
        bands(c, new int[]{0x7fa6c7, 0x8fb5d3, 0x9fc2dc, 0xb1cfe4, 0xc3dbeb, 0xd5e7f1}, 0, 150);
        
        ridge(c, x -> 104 + (int) (22 * Math.abs(Math.sin(x * 0.021 + 0.4)) + 6 * Math.sin(x * 0.09)), 0xc8d9e6, 0xe4eef5);
        
        int[][] peaks = {{60, 70}, {170, 58}, {300, 66}, {400, 80}};
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
        ridge(c, x -> 160 + (int) (5 * Math.sin(x * 0.03) + 3 * Math.sin(x * 0.08)), 0xe6eff6, 0xffffff);
        ridge(c, x -> 188 + (int) (6 * Math.sin(x * 0.02 + 2)), 0xf2f7fb, 0xffffff);
        
        for (int i = 0; i < 90; i++) {
            int x = rnd.nextInt(c.width());
            int y = 192 + rnd.nextInt(48);
            c.rect(x, y, 5 + rnd.nextInt(6), 1, 0xd4e3ef);
        }
        
        int[] treesX = {24, 110, 138, 250, 372, 410};
        for (int i = 0; i < treesX.length; i++) {
            pine(c, treesX[i], 176 + (i % 3) * 6, 10 + (i % 2) * 4);
        }
        for (int i = 0; i < 140; i++) {
            c.set(rnd.nextInt(c.width()), rnd.nextInt(170), 0xffffff);
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
        
        c.ellipse(36, 98, 10, 8, fur);
        c.ellipse(68, 98, 10, 8, fur);
        
        c.ellipse(16, 70, 10, 24, fur);
        c.ellipse(88, 70, 10, 24, fur);
        for (int i = 0; i < 3; i++) {
            c.line(10 + i * 5, 92, 9 + i * 5, 99, 2, 0x5a6a7a);
            c.line(82 + i * 5, 92, 83 + i * 5, 99, 2, 0x5a6a7a);
        }
        
        c.ellipse(52, 66, 32, 32, fur);
        
        c.line(36, 22, 28, 12, 4, 0x8a7a6a);
        c.line(28, 12, 30, 4, 3, 0x8a7a6a);
        c.line(68, 22, 76, 12, 4, 0x8a7a6a);
        c.line(76, 12, 74, 4, 3, 0x8a7a6a);
        
        c.ellipse(52, 32, 21, 18, fur);
        
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
        for (int i = 0; i < 60; i++) {
            int x = rnd.nextInt(c.width());
            int y = rnd.nextInt(c.height());
            if (c.get(x, y) == Pix.CLEAR && (c.get(x + 1, y) == fur || c.get(x - 1, y) == fur)) {
                c.set(x, y, fur);
            }
        }
        
        for (int i = 0; i < 40; i++) {
            int x = 24 + rnd.nextInt(56);
            int y = 40 + rnd.nextInt(50);
            if (c.get(x, y) == fur && c.get(x + 1, y + 1) == fur) {
                c.set(x, y, shade);
                c.set(x + 1, y + 1, shade);
            }
        }
        
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
        
        c.line(52, 58, 52, 74, 1, 0x9fd6f2);
        c.line(44, 66, 60, 66, 1, 0x9fd6f2);
        c.line(46, 60, 58, 72, 1, 0x9fd6f2);
        c.line(58, 60, 46, 72, 1, 0x9fd6f2);
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




    static void skyBg(Pix c, Random rnd) {
        bands(c, new int[]{0x140a1f, 0x1c0d2a, 0x261135, 0x321540, 0x40194a, 0x521c50, 0x681f52, 0x7e2550}, 0, 240);
        for (int i = 0; i < 90; i++) {
            c.set(rnd.nextInt(c.width()), rnd.nextInt(130), i % 4 == 0 ? 0xe8d0f0 : 0x9a7aac);
        }
        
        c.ellipse(320, 62, 36, 36, 0x9e3029);
        c.ellipse(318, 60, 34, 34, 0xc9453a);
        c.ellipse(310, 50, 16, 14, 0xe0634e);
        c.ellipse(334, 72, 6, 5, 0x9e3029);
        c.ellipse(304, 78, 4, 3, 0x9e3029);
        c.ellipse(340, 48, 3, 3, 0xa8362d);
        
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
        
        wing(c, 44, 40, -1, wing, bone);
        wing(c, 74, 40, 1, wing, bone);
        
        for (int i = 0; i < 12; i++) {
            int x = 34 + i * 4 + rnd.nextInt(3);
            int h = 10 + rnd.nextInt(14);
            c.rect(x, 100 - h, 4, h, 0xff5a2a);
            c.rect(x + 1, 104 - h / 2, 2, h / 2, 0xffb23a);
        }
        c.ellipse(59, 102, 28, 8, 0xd9402a);
        
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
        
        c.line(42, 50, 26, 80, 7, skin);
        for (int i = 0; i < 3; i++) {
            c.line(24 + i * 3, 82, 22 + i * 3, 89, 2, 0xe8dcc0);
        }
        c.line(76, 50, 94, 74, 7, skin);
        c.ellipse(98, 78, 9, 9, 0x6a2ab8);
        c.ellipse(96, 76, 5, 5, 0x9a4ad8);
        c.rect(94, 73, 2, 2, 0xe8c8ff);
        
        c.line(50, 22, 38, 10, 5, 0xe8dcc0);
        c.line(38, 10, 34, 0, 3, 0xe8dcc0);
        c.line(68, 22, 80, 10, 5, 0xe8dcc0);
        c.line(80, 10, 84, 0, 3, 0xe8dcc0);
        c.ellipse(59, 32, 14, 15, skin);
        c.ellipse(55, 26, 5, 5, skinLight);
        
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

    static void wing(Pix c, int sx, int sy, int dir, int membrane, int bone) {
        int[][] tips = {{sx + dir * 42, sy - 26}, {sx + dir * 46, sy + 6}, {sx + dir * 36, sy + 30}, {sx + dir * 20, sy + 40}};
        for (int i = 0; i + 1 < tips.length; i++) {
            fillTriangle(c, sx, sy, tips[i][0], tips[i][1], tips[i + 1][0], tips[i + 1][1], membrane);
        }
        fillTriangle(c, sx, sy, sx + dir * 30, sy - 34, tips[0][0], tips[0][1], membrane);
        for (int[] t : tips) {
            c.line(sx, sy, t[0], t[1], 2, bone);
        }
        c.line(sx, sy, sx + dir * 30, sy - 34, 3, bone);
        c.line(sx + dir * 30, sy - 34, tips[0][0], tips[0][1], 2, bone);
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

    static class Pix {


        public static final int CLEAR = -1;

        private final int w;
        private final int h;
        private final int[] px;

        Pix(int w, int h) {
            this.w = w;
            this.h = h;
            this.px = new int[w * h];
            java.util.Arrays.fill(px, CLEAR);
        }

        public int width() {
            return w;
        }

        public int height() {
            return h;
        }

        public boolean inside(int x, int y) {
            return x >= 0 && y >= 0 && x < w && y < h;
        }

        public void set(int x, int y, int rgb) {
            if (inside(x, y)) {
                px[y * w + x] = rgb;
            }
        }

        public int get(int x, int y) {
            return inside(x, y) ? px[y * w + x] : CLEAR;
        }

        public void rect(int x, int y, int rw, int rh, int rgb) {
            for (int yy = y; yy < y + rh; yy++) {
                for (int xx = x; xx < x + rw; xx++) {
                    set(xx, yy, rgb);
                }
            }
        }

        public void ellipse(double cx, double cy, double rx, double ry, int rgb) {
            for (int yy = (int) Math.floor(cy - ry); yy <= (int) Math.ceil(cy + ry); yy++) {
                for (int xx = (int) Math.floor(cx - rx); xx <= (int) Math.ceil(cx + rx); xx++) {
                    double dx = (xx + 0.5 - cx) / rx;
                    double dy = (yy + 0.5 - cy) / ry;
                    if (dx * dx + dy * dy <= 1.0) {
                        set(xx, yy, rgb);
                    }
                }
            }
        }

            public void line(int x0, int y0, int x1, int y1, int thick, int rgb) {
            int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
            for (int i = 0; i <= steps; i++) {
                double t = steps == 0 ? 0 : (double) i / steps;
                int x = (int) Math.round(x0 + (x1 - x0) * t);
                int y = (int) Math.round(y0 + (y1 - y0) * t);
                rect(x - thick / 2, y - thick / 2, thick, thick, rgb);
            }
        }

            public void tint(int x, int y, double factor) {
            int c = get(x, y);
            if (c != CLEAR) {
                set(x, y, scale(c, factor));
            }
        }

        public static int scale(int rgb, double f) {
            int r = clamp((int) (((rgb >> 16) & 0xff) * f));
            int g = clamp((int) (((rgb >> 8) & 0xff) * f));
            int b = clamp((int) ((rgb & 0xff) * f));
            return (r << 16) | (g << 8) | b;
        }

        public static int mix(int a, int b, double t) {
            int r = (int) (((a >> 16) & 0xff) * (1 - t) + ((b >> 16) & 0xff) * t);
            int g = (int) (((a >> 8) & 0xff) * (1 - t) + ((b >> 8) & 0xff) * t);
            int bl = (int) ((a & 0xff) * (1 - t) + (b & 0xff) * t);
            return (clamp(r) << 16) | (clamp(g) << 8) | clamp(bl);
        }

        private static int clamp(int v) {
            return Math.max(0, Math.min(255, v));
        }

            public void outline(int rgb) {
            int[] copy = px.clone();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (copy[y * w + x] != CLEAR) {
                        continue;
                    }
                    if (opaque(copy, x - 1, y) || opaque(copy, x + 1, y) || opaque(copy, x, y - 1) || opaque(copy, x, y + 1)) {
                        px[y * w + x] = rgb;
                    }
                }
            }
        }

        private boolean opaque(int[] a, int x, int y) {
            return inside(x, y) && a[y * w + x] != CLEAR;
        }

            public void draw(Pix src, int dx, int dy) {
            for (int y = 0; y < src.h; y++) {
                for (int x = 0; x < src.w; x++) {
                    int c = src.px[y * src.w + x];
                    if (c != CLEAR) {
                        set(dx + x, dy + y, c);
                    }
                }
            }
        }

        public BufferedImage toImage(int scale) {
            BufferedImage img = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = px[y * w + x];
                    int argb = c == CLEAR ? 0 : 0xff000000 | c;
                    for (int sy = 0; sy < scale; sy++) {
                        for (int sx = 0; sx < scale; sx++) {
                            img.setRGB(x * scale + sx, y * scale + sy, argb);
                        }
                    }
                }
            }
            return img;
        }

    }
}
