package kr.ac.jbnu.se.tetris.network.lobby;

import java.io.IOException;
import java.util.List;

/** 로비 방 목록·입장·퇴장 등 서버 요청만 담당한다. UI·Swing 코드 없음. */
public final class LobbyRoomFlow {

	private LobbyRoomFlow() {
	}

	public static List<RoomSnapshot> listRooms(LobbyClient client) throws IOException {
		return client.listRooms();
	}

	public static LobbyMessage fetchMyRoom(LobbyClient client) throws IOException {
		return client.fetchMyRoom();
	}

	public static LobbyMessage createRoom(LobbyClient client, String name, RoomVisibility visibility) throws IOException {
		return client.createRoom(name, visibility);
	}

	public static LobbyMessage joinRoom(LobbyClient client, String roomId, String joinCodeOrNull) throws IOException {
		return client.joinRoom(roomId, joinCodeOrNull);
	}

	public static void leaveRoom(LobbyClient client) throws IOException {
		client.leaveRoom();
	}

	public static LobbyMessage startFreshGame(LobbyClient client) throws IOException {
		return client.startGame();
	}

	public static LobbyMessage resumeCoopGame(LobbyClient client, String coopId, long seed, String snapshotJson)
			throws IOException {
		return client.resumeCoop(coopId, seed, snapshotJson);
	}

	public static String formatRoomListLine(RoomSnapshot r) {
		String lock = r.visibility == RoomVisibility.PRIVATE ? "[비밀]" : "[공개]";
		String state = r.joinable ? "입장 가능" : "만원";
		return String.format("%s %s · %s · %d/%d · %s", lock, r.name, r.hostNick, r.playerCount, r.maxPlayers,
				state);
	}
}