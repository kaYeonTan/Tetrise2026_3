package kr.ac.jbnu.se.tetris.view;

import java.awt.Color;

// F-05 그리기 옵션 (회색조·줄 깜빡임)
public final class RenderOptions {

	private RenderOptions() {
	}

	public static Color pieceColor(Color base, boolean grayscale) {
		if (!grayscale)
			return base;
		int g = (base.getRed() + base.getGreen() + base.getBlue()) / 3;
		return new Color(g, g, g);
	}
}
