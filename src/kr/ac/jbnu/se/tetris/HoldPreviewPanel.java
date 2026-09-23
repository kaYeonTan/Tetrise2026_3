package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import kr.ac.jbnu.se.tetris.core.GameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;

// 홀드 칸 미리보기
public class HoldPreviewPanel extends JPanel {

	GameEngine engine;
	final int cell = 14;

	public HoldPreviewPanel(GameEngine engine) {
		this.engine = engine;
		setPreferredSize(new Dimension(80, 90));
		setBackground(new Color(40, 40, 40));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (engine == null || !engine.isStarted())
			return;

		GameSession s = engine.getSession(0);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("HOLD", 16, 14);

		if (s.hold.isEmpty())
			return;

		Shape shape = new Shape();
		shape.setShape(s.hold.getStored());

		PreviewDraw.drawPiece(g, shape, s.hold.getStored(), 22, 28, cell);
	}
}
