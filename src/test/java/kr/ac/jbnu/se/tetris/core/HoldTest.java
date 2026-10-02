package kr.ac.jbnu.se.tetris.core;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.piece.RotationState;
import kr.ac.jbnu.se.tetris.piece.SevenBagRandomizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HoldTest {

	@Test
	void cannotHoldTwiceBeforeLock() {
		GameSession s = new GameSession(0);
		s.randomizer = new SevenBagRandomizer(1L);
		s.spawnPiece();
		assertTrue(s.tryHold());
		assertFalse(s.tryHold());
	}

	@Test
	void spawnFromHoldUsesSpawnRotation() {
		GameSession s = new GameSession(0);
		s.hold.setStored(Tetrominoes.TShape);
		assertTrue(s.spawnShape(Tetrominoes.TShape));
		assertEquals(RotationState.SPAWN, s.curPiece.getRotationState());
	}
}
