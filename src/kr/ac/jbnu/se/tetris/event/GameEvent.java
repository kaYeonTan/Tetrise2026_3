package kr.ac.jbnu.se.tetris.event;

public class GameEvent {

	public GameEventType type;
	public int playerIndex;
	public int value;

	public GameEvent(GameEventType type, int playerIndex, int value) {
		this.type = type;
		this.playerIndex = playerIndex;
		this.value = value;
	}

	public GameEvent(GameEventType type, int playerIndex) {
		this(type, playerIndex, 0);
	}
}
