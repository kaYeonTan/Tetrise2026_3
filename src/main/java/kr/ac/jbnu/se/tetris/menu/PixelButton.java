package kr.ac.jbnu.se.tetris.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** 라벨과 영역(bounds)을 한 객체로 묶어 그리기와 클릭 판정이 같은 좌표를 쓰게 하는 픽셀 버튼. */
public record PixelButton(String label, Rectangle bounds) {

    public boolean contains(int x, int y) {
        return bounds.contains(x, y);
    }

    /** type 1=빨강, 2=보라, 그 외=기본(베이지). */
    public void draw(Graphics2D g, Font font, boolean hover, int type, float size) {
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;
        g.setColor(new Color(0x0c090d));
        g.fillRect(x, y + 5, w, h);
        g.setColor(new Color(0x1a1216));
        g.fillRect(x, y, w, h);
        Color fill;
        Color textColor;
        switch (type) {
            case 1:
                fill = hover ? new Color(0xc24536) : new Color(0xa8352a);
                textColor = new Color(0xfff4e6);
                break;
            case 2:
                fill = hover ? new Color(0x4a3f52) : new Color(0x3a3040);
                textColor = new Color(0xf0e2c8);
                break;
            default:
                fill = hover ? new Color(0xefe0bf) : new Color(0xd9c29a);
                textColor = new Color(0x1a1216);
                break;
        }
        g.setColor(fill);
        g.fillRect(x + 4, y + 4, w - 8, h - 8);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(x + 4, y + 4, w - 8, 3);
        g.setFont(font.deriveFont(size));
        int tw = g.getFontMetrics().stringWidth(label);
        g.setColor(textColor);
        g.drawString(label, x + (w - tw) / 2, y + h / 2 + (int) (size / 2) - 2);
    }
}
