package kr.ac.jbnu.se.tetris.effect;
import java.awt.Graphics2D;
import kr.ac.jbnu.se.tetris.BossAwakeningArt;
public final class GrassAwakening extends AwakeningEffect {
    public GrassAwakening() { super(3); }
    protected void drawParticles(Graphics2D g, int passed, double progress, int bossX) {
        BossAwakeningArt.drawMeadow(g, passed);
    }
}
