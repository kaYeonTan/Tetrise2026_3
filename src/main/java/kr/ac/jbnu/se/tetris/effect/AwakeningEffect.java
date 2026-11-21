package kr.ac.jbnu.se.tetris.effect;
import java.awt.Graphics2D;
public abstract class AwakeningEffect {
    private final int stage;
    protected AwakeningEffect(int stage) { this.stage = stage; }
    public final int duration() { return stage == 5 ? 75 : 70; }
    public final void draw(Graphics2D g, int remaining, int bossX) {
        int passed = duration() - remaining;
        double progress = (double) passed / duration();
        drawParticles(g, passed, progress, bossX);
    }
    protected abstract void drawParticles(Graphics2D g, int passed, double progress, int bossX);
    public static AwakeningEffect forStage(int stage) {
        return switch (stage) {
            case 1 -> new DesertAwakening(); case 2 -> new IslandAwakening();
            case 3 -> new GrassAwakening(); case 4 -> new SnowAwakening();
            case 5 -> new SkyAwakening();
            default -> new AwakeningEffect(stage) {
                protected void drawParticles(Graphics2D g, int passed, double progress, int bossX) {
                    // 해당하지 않는 스테이지에는 입자 효과가 없음
                }
            };
        };
    }
}
