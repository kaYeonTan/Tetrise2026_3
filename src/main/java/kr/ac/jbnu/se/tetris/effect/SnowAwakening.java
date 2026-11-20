package kr.ac.jbnu.se.tetris.effect;
import java.awt.Graphics2D;
import kr.ac.jbnu.se.tetris.BossAwakeningArt;
public final class SnowAwakening extends AwakeningEffect {
    public SnowAwakening() { super(4); }
    protected void drawParticles(Graphics2D g, int passed, double progress, int bossX) {
        BossAwakeningArt.drawSnow(g, passed);
    }
}
