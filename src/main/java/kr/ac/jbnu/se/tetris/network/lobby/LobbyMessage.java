package kr.ac.jbnu.se.tetris.network.lobby;

import java.util.ArrayList;
import java.util.List;

/** JSON 한 줄 프로토콜 (type 필드로 구분). */
public class LobbyMessage {

	public String type;

	public String nick;
	public String clientId;

	public String name;
	public String visibility;
	public String roomId;
	public String joinCode;

	public String message;
	public String code;

	public String roomName;
	public String role;
	public List<String> players = new ArrayList<>();
	public List<RoomSnapshot> rooms = new ArrayList<>();

	/** in-game */
	public int playerIndex;
	public String action;
	public String coopId;
	public long seed;
	public String snapshotJson;
	public int saveSlot;
	public boolean resumeGame;

	public static LobbyMessage of(LobbyMessageType t) {
		LobbyMessage m = new LobbyMessage();
		m.type = t.name();
		return m;
	}
}
