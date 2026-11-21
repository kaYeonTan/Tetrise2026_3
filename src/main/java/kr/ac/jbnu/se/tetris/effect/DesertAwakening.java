package kr.ac.jbnu.se.tetris.effect;
import java.awt.Graphics2D;
import kr.ac.jbnu.se.tetris.BossAwakeningArt;
public final class DesertAwakening extends AwakeningEffect {
    public DesertAwakening() { super(1); }
    protected void drawParticles(Graphics2D g, int passed, double progress, int bossX) {
        BossAwakeningArt.drawDesert(g, passed, bossX);
    }
}
