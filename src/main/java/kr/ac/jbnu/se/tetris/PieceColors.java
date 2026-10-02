package kr.ac.jbnu.se.tetris;

import java.awt.Color;

// 블록 색 (펜토미노 포함)
public class PieceColors {

	static final Color[] COLORS = {
			new Color(0, 0, 0),
			new Color(0xe0524a), // Z
			new Color(0x5cc05a), // S
			new Color(0x4fc3d9), // I
			new Color(0xa45ad8), // T
			new Color(0xf2c94c), // O
			new Color(0xf0943a), // L
			new Color(0x4a6ee0), // J
			new Color(0xc06be8), new Color(0xc06be8), new Color(0xc06be8),
			new Color(0xc06be8), new Color(0xc06be8)
	};

	public static Color of(Tetrominoes shape) {
		if (shape == null || shape.ordinal() >= COLORS.length)
			return Color.GRAY;
		return COLORS[shape.ordinal()];
	}
}
