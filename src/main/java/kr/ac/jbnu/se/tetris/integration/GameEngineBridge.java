package kr.ac.jbnu.se.tetris.integration;

import kr.ac.jbnu.se.tetris.core.GameSession;

public interface GameEngineBridge {

	boolean hasActiveEngine();

	GameSession session(int playerIndex);

	boolean isLineClearBlinkOn(int playerIndex);
}
