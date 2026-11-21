package kr.ac.jbnu.se.tetris;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;

public class Art {

    // 도트 그림 데이터(o: 겉불꽃, m: 중간 불꽃, c: 속불꽃, .: 투명)라 맞춤법 검사 제외
    @SuppressWarnings("SpellCheckingInspection")
    public static final String FLAME_CORE_ROW = "ommcmmo";
    // 도트 그림 데이터(o: 겉불꽃, m: 중간 불꽃, c: 속불꽃, .: 투명)라 맞춤법 검사 제외
    @SuppressWarnings("SpellCheckingInspection")
    public static final String FLAME_BODY_ROW = ".ommmo.";
    public enum MonsterPose {
        IDLE, HIT, DEATH
    }

    private static final int STAGE_SLOTS = 6;
    static final int SCREEN_CENTER_X = 640;
    private static final int FLAME_FRAME_COUNT = 2;
    private static final int FLAME_WIDTH = 7;
    private static final int FLAME_HEIGHT = 11;
    private static final String[] IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg"};

    public static final int CLOUD_LIGHT = 0xffffff;
    public static final int CLOUD_SHADE = 0xdcecf6;
    public static final int WING_ROOT_Y = 40;
    private static final BufferedImage[] backgroundCache = new BufferedImage[STAGE_SLOTS];
    private static final BufferedImage[] proceduralMonsterCache = new BufferedImage[STAGE_SLOTS];
    private static final BufferedImage[][] monsterSpriteCache =
            new BufferedImage[STAGE_SLOTS][MonsterPose.values().length];
    private static BufferedImage[] flameFrames;

    private static final kr.ac.jbnu.se.tetris.art.StageArt[] STAGE_ART = {
        null, new kr.ac.jbnu.se.tetris.art.DesertArt(), new kr.ac.jbnu.se.tetris.art.IslandArt(),
        new kr.ac.jbnu.se.tetris.art.GrassArt(), new kr.ac.jbnu.se.tetris.art.SnowArt(),
        new kr.ac.jbnu.se.tetris.art.SkyArt()
    };

    public static BufferedImage background(int stage) {
        if (backgroundCache[stage] == null) {
            backgroundCache[stage] = loadBackgroundFile(stage);
        }
        if (backgroundCache[stage] == null) {
            backgroundCache[stage] = proceduralBackground(stage);
        }
        return backgroundCache[stage];
    }

    static BufferedImage loadBackgroundFile(int stage) {
        if (stage < 1 || stage > 5) {
            return null;
        }
        return readFirstExistingImage("/images/background/stage" + stage);
    }

    /** 확장자(png, jpg, jpeg)를 차례로 붙여 처음 읽히는 이미지를 돌려준다. 없으면 null. */
    private static BufferedImage readFirstExistingImage(String pathWithoutExtension) {
        for (String extension : IMAGE_EXTENSIONS) {
            BufferedImage image = readResourceImage(pathWithoutExtension + extension);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    static BufferedImage proceduralBackground(int stage) {
        return STAGE_ART[stage == 0 ? 5 : stage].background(stage);
    }

    public static BufferedImage monster(int stage, MonsterPose pose) {
        stage = Math.max(1, Math.min(5, stage));
        int poseIndex = pose.ordinal();
        int idleIndex = MonsterPose.IDLE.ordinal();
        BufferedImage[] stageSprites = monsterSpriteCache[stage];
        if (stageSprites[poseIndex] == null) {
            stageSprites[poseIndex] = loadMonsterFile(stage, pose);
        }
        if (stageSprites[poseIndex] != null) {
            return stageSprites[poseIndex];
        }
        if (pose != MonsterPose.IDLE && stageSprites[idleIndex] != null) {
            return stageSprites[idleIndex];
        }
        return proceduralMonster(stage);
    }

    static BufferedImage loadMonsterFile(int stage, MonsterPose pose) {
        String poseName = pose.name().toLowerCase();
        BufferedImage image = readFirstExistingImage("/images/boss/stage" + stage + "/" + poseName);
        if (image != null) {
            return flipHorizontal(solidifySpriteAlpha(image));
        }
        if (pose == MonsterPose.DEATH) {
            System.err.println("Art: could not load boss death sprite for stage " + stage);
        }
        return null;
    }

    static BufferedImage toArgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            return src;
        }
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        AffineTransform flip = AffineTransform.getScaleInstance(-1.0, 1.0);
        flip.translate(-w, 0);
        g.drawImage(src, flip, null);
        g.dispose();
        return flipped;
    }

    /** Fixes nukki/feather exports: unpremultiply and force visible pixels to opaque alpha. */
    static BufferedImage solidifySpriteAlpha(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int alpha = argb >>> 24;
            if (alpha <= 24) {
                pixels[i] = 0;
                continue;
            }
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;
            r = Math.min(255, (r * 255 + alpha / 2) / alpha);
            g = Math.min(255, (g * 255 + alpha / 2) / alpha);
            b = Math.min(255, (b * 255 + alpha / 2) / alpha);
            pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, w, h, pixels, 0, w);
        return out;
    }

    static BufferedImage readResourceImage(String resourcePath) {
        try (InputStream in = Art.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                return null;
            }
            return toArgb(img);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    static BufferedImage proceduralMonster(int stage) {
        if (proceduralMonsterCache[stage] == null) {
            proceduralMonsterCache[stage] = flipHorizontal(toArgb(STAGE_ART[stage].monster(stage)));
        }
        return proceduralMonsterCache[stage];
    }

    public static void effect(Graphics2D g, int stage, int w, int h, long time) {
        if (stage >= 1 && stage <= 5) STAGE_ART[stage].effect(g, w, h, time);
    }

    public static BufferedImage flame(int frame) {
        if (flameFrames == null) {
            flameFrames = buildFlameFrames();
        }
        return flameFrames[frame % FLAME_FRAME_COUNT];
    }

    // 도트 그림 데이터(o: 겉불꽃, m: 중간 불꽃, c: 속불꽃, .: 투명)라 맞춤법 검사 제외
    @SuppressWarnings("SpellCheckingInspection")
    private static BufferedImage[] buildFlameFrames() {
        String[][] shapes = {
            {"...o...", "...o...", "..oo...", "..ooo..", ".oomo..", FLAME_BODY_ROW,
                    FLAME_BODY_ROW, "oommmo.", FLAME_CORE_ROW, FLAME_CORE_ROW, ".omcmo."},
            {"....o..", "...oo..", "...oo..", "..ooo..", "..omoo.", FLAME_BODY_ROW,
             ".ommmoo", ".ommmoo", FLAME_CORE_ROW, FLAME_CORE_ROW, ".omcmo."}
        };
        BufferedImage[] frames = new BufferedImage[FLAME_FRAME_COUNT];
        for (int frame = 0; frame < FLAME_FRAME_COUNT; frame++) {
            Pix canvas = new Pix(FLAME_WIDTH, FLAME_HEIGHT);
            for (int y = 0; y < FLAME_HEIGHT; y++) {
                for (int x = 0; x < FLAME_WIDTH; x++) {
                    paintFlamePixel(canvas, x, y, shapes[frame][y].charAt(x));
                }
            }
            frames[frame] = canvas.toImage(3);
        }
        return frames;
    }

    /** o = 겉불꽃, m = 중간, c = 속불꽃, 그 외는 투명. */
    private static void paintFlamePixel(Pix canvas, int x, int y, char symbol) {
        if (symbol == 'o') canvas.set(x, y, 0xe8622a);
        if (symbol == 'm') canvas.set(x, y, 0xf7a531);
        if (symbol == 'c') canvas.set(x, y, 0xffe9a3);
    }


    static final int[][] TORCHES = {{177, 86}, {409, 86}};

    private static BufferedImage castleCache;


    static BufferedImage castle() {
        if (castleCache == null) {
            castleCache = paintCastle().toImage(3);
        }
        return castleCache;
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

        drawCastleMasonry(c, rnd, stones, floors, brickW, brickH, floorY, shade);
        for (int[] t : TORCHES) {
            drawSconce(c, t[0], t[1]);
        }
        return c;
    }

    private static void drawCastleMasonry(Pix c, Random rnd, int[] stones, int[] floors,
                                          int brickW, int brickH, int floorY,
                                          java.util.Map<Long, Integer> shade) {
        for (int y = 0; y < 240; y++) {
            for (int x = 0; x < 427; x++) {
                int col = y >= floorY
                        ? castleFloorColor(x, y, floorY, floors, shade, rnd)
                        : castleWallColor(x, y, brickW, brickH, stones, shade, rnd);
                c.set(x, y, light(col, x, y));
            }
        }
    }

    private static int castleFloorColor(int x, int y, int floorY, int[] floors,
                                        java.util.Map<Long, Integer> shade, Random rnd) {
        int fy = y - floorY;
        int row = fy / 9;
        int off = (row % 2) * 16;
        long key = (1L << 40) | ((long) row << 20) | ((x + off) / 32);
        int col = shade.computeIfAbsent(key, k -> floors[rnd.nextInt(floors.length)]);
        if (fy % 9 == 0 || (x + off) % 32 == 0) col = 0x0e0c11;
        if (y == floorY) col = 0x4a4252;
        return col;
    }

    private static int castleWallColor(int x, int y, int brickW, int brickH, int[] stones,
                                       java.util.Map<Long, Integer> shade, Random rnd) {
        int row = y / brickH;
        int off = (row % 2) * (brickW / 2);
        long key = ((long) row << 20) | ((x + off) / brickW);
        int col = shade.computeIfAbsent(key, k -> stones[rnd.nextInt(stones.length)]);
        int ly = y % brickH;
        int lx = (x + off) % brickW;
        if (ly == 0 || lx == 0) col = 0x131017;
        else if (ly == 1 || lx == 1) col = Pix.scale(col, 1.18);
        else if (ly == brickH - 1 || lx == brickW - 1) col = Pix.scale(col, 0.82);
        return col;
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

    public static void bands(Pix c, int[] colors, int top, int bottom) {
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

    public static void ridge(Pix c, java.util.function.IntUnaryOperator topAt, int fill, int rim) {
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

    public static void cloud(Pix c, int cx, int cy, int size) {
        c.ellipse(cx, cy + 2, size * 1.6, size * 0.6, CLOUD_SHADE);
        c.ellipse(cx - size * 0.6, cy, size * 0.7, size * 0.55, CLOUD_LIGHT);
        c.ellipse(cx + size * 0.2, cy - size * 0.3, size * 0.8, size * 0.7, CLOUD_LIGHT);
        c.ellipse(cx + size * 0.9, cy + 1, size * 0.6, size * 0.45, CLOUD_LIGHT);
        c.rect(cx - size, cy + (int) (size * 0.45), size * 2, 1,  CLOUD_SHADE);
    }



































































    public static class Pix {


        public static final int CLEAR = -1;

        private final int w;
        private final int h;
        private final int[] px;

        public Pix(int w, int h) {
            this.w = w;
            this.h = h;
            this.px = new int[w * h];
            Arrays.fill(px, CLEAR);
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
