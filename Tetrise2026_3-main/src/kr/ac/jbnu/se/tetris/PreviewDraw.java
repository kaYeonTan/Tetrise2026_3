package kr.ac.jbnu.se.tetris;

import java.awt.Graphics;

// HOLD / NEXT 그릴 때 보드랑 같은 방향 (y 부호)
public class PreviewDraw {

	public static void drawPiece(Graphics g, Shape shape, Tetrominoes type, int ox, int oy, int cell) {
		int n = shape.blockCount();
		for (int i = 0; i < n; i++) {
			int px = shape.x(i);
			int py = shape.y(i);
			int sx = ox + px * cell;
			int sy = oy + py * cell;
			drawCell(g, sx, sy, type, cell);
		}
	}

	static void drawCell(Graphics g, int x, int y, Tetrominoes shape, int cell) {
		g.setColor(PieceColors.of(shape));
		g.fillRect(x, y, cell - 2, cell - 2);
	}
}
