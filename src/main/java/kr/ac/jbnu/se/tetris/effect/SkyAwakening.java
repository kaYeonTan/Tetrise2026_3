package kr.ac.jbnu.se.tetris.effect;
import java.awt.Graphics2D;
import kr.ac.jbnu.se.tetris.BossAwakeningArt;
public final class SkyAwakening extends AwakeningEffect {
    public SkyAwakening() { super(5); }
    protected void drawParticles(Graphics2D g, int passed, double progress, int bossX) {
        BossAwakeningArt.drawCastle(g, passed);
    }
}
