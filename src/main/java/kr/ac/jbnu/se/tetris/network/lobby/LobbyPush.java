package kr.ac.jbnu.se.tetris.network.lobby;

/** 서버 → 클라이언트 푸시 메시지 구분. */
public final class LobbyPush {

	private LobbyPush() {
	}

	public static boolean isPush(String type) {
		if (type == null) {
			return false;
		}
		return LobbyMessageType.GAME_STARTED.name().equals(type)
				|| LobbyMessageType.COOP_SNAPSHOT_PUSH.name().equals(type)
				|| LobbyMessageType.PLAYER_ACTION.name().equals(type)
				|| LobbyMessageType.PEER_DISCONNECTED.name().equals(type);
	}
}
