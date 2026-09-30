package kr.ac.jbnu.se.tetris.core;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.event.GameEventBus;
import kr.ac.jbnu.se.tetris.event.GameEventType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameEngineEventTest {

	@Test
	void gameOverFiresOnce() {
		GameEventBus bus = new GameEventBus();
		GameEngine engine = new GameEngine(bus);
		final int[] overCount = { 0 };
		bus.subscribe(GameEventType.GAME_OVER, e -> overCount[0]++);

		engine.start();
		GameSession s = engine.getSession(0);
		for (int i = 0; i < s.cells.length; i++)
			s.cells[i] = Tetrominoes.SquareShape;

		if (!s.spawnPiece())
			engine.handleGameOver();
		engine.handleGameOver();

		assertTrue(!engine.isStarted());
		assertEquals(1, overCount[0]);
	}

	@Test
	void requestedStageSurvivesStart() {
		GameEngine engine = new GameEngine();
		engine.setStage(4);
		engine.start();
		assertEquals(4, engine.getStage());
	}
}
