package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import kr.ac.jbnu.se.tetris.core.GameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.piece.SevenBagRandomizer;

// 다음 블록 3개 미리보기
public class NextPreviewPanel extends JPanel {

	GameEngine engine;
	final int cell = 14;
	final int showCount = SevenBagRandomizer.PREVIEW_COUNT;

	public NextPreviewPanel(GameEngine engine) {
		this.engine = engine;
		setPreferredSize(new Dimension(80, 200));
		setBackground(new Color(40, 40, 40));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (engine == null || !engine.isStarted())
			return;

		GameSession s = engine.getSession(0);
		if (s.randomizer == null)
			return;

		g.setColor(Color.LIGHT_GRAY);
		g.drawString("NEXT", 18, 14);

		for (int slot = 0; slot < showCount; slot++) {
			Tetrominoes next = s.randomizer.peekAt(slot);
			if (next == Tetrominoes.NoShape)
				continue;

			Shape shape = new Shape();
			shape.setShape(next);

			int oy = 20 + slot * 58;
			PreviewDraw.drawPiece(g, shape, next, 22, oy, cell);
		}
	}
}
