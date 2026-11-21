package kr.ac.jbnu.se.tetris.art;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
public interface StageArt {
    BufferedImage background(int stage);
    BufferedImage monster(int stage);
    void effect(Graphics2D g, int width, int height, long time);
}
