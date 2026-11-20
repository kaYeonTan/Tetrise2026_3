package kr.ac.jbnu.se.tetris.render;
import java.awt.Graphics2D;
import java.util.function.Consumer;
public final class BackdropRenderer implements GameLayer {
    private final Consumer<Graphics2D> draw;
    public BackdropRenderer(Consumer<Graphics2D> draw) { this.draw = draw; }
    @Override public void draw(Graphics2D g) { draw.accept(g); }
}
