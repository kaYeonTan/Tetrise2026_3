package kr.ac.jbnu.se.tetris.network.lobby;

/** 로비 서버 상수 (팀 공통). */
public final class LobbyConfig {

	public static final int DEFAULT_PORT = 8765;
	public static final int MAX_ROOMS = 5;
	public static final int MAX_PLAYERS_PER_ROOM = 2;
	/** 비밀방 입장 코드 길이 (서버가 생성). */
	public static final int JOIN_CODE_LENGTH = 6;

	private LobbyConfig() {
	}
}
