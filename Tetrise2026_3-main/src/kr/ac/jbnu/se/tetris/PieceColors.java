package kr.ac.jbnu.se.tetris;

import java.awt.Color;

// 블록 색 (펜토미노 포함)
public class PieceColors {

	static final Color[] COLORS = {
			new Color(0, 0, 0),
			new Color(204, 102, 102), new Color(102, 204, 102), new Color(102, 102, 204),
			new Color(204, 204, 102), new Color(204, 102, 204), new Color(102, 204, 204),
			new Color(218, 170, 0),
			new Color(180, 120, 220), new Color(255, 160, 80), new Color(80, 200, 180),
			new Color(200, 200, 80), new Color(160, 100, 200)
	};

	public static Color of(Tetrominoes shape) {
		if (shape == null || shape.ordinal() >= COLORS.length)
			return Color.GRAY;
		return COLORS[shape.ordinal()];
	}
}
