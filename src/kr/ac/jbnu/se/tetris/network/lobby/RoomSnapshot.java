package kr.ac.jbnu.se.tetris.network.lobby;

/** 방 목록·로비 UI용 (비밀방은 joinCode 미포함). */
public class RoomSnapshot {

	public String roomId;
	public String name;
	public RoomVisibility visibility;
	public String hostNick;
	public int playerCount;
	public int maxPlayers;
	public boolean joinable;

	public RoomSnapshot() {
	}

	public RoomSnapshot(String roomId, String name, RoomVisibility visibility, String hostNick, int playerCount,
			int maxPlayers, boolean joinable) {
		this.roomId = roomId;
		this.name = name;
		this.visibility = visibility;
		this.hostNick = hostNick;
		this.playerCount = playerCount;
		this.maxPlayers = maxPlayers;
		this.joinable = joinable;
	}
}
