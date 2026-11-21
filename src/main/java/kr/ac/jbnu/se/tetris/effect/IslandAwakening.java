package kr.ac.jbnu.se.tetris.effect;
import java.awt.Graphics2D;
import kr.ac.jbnu.se.tetris.BossAwakeningArt;
public final class IslandAwakening extends AwakeningEffect {
    public IslandAwakening() { super(2); }
    protected void drawParticles(Graphics2D g, int passed, double progress, int bossX) {
        BossAwakeningArt.drawIsland(g, passed, bossX);
    }
}
