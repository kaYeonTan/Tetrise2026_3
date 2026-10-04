package kr.ac.jbnu.se.tetris.ui.router;

import kr.ac.jbnu.se.tetris.core.GameMode;

public class GameParams implements RouteParams {

	public final GameMode mode;
	public final int stage;
	public final boolean resume;

	public GameParams(GameMode mode, int stage, boolean resume) {
		this.mode = mode;
		this.stage = stage;
		this.resume = resume;
	}

	public GameParams(GameMode mode) {
		this(mode, 1, false);
	}
}
