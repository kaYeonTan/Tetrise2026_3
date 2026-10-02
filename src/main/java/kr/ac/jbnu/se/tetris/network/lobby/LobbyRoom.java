package kr.ac.jbnu.se.tetris.network.lobby;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LobbyRoom {

	public final String roomId;
	public final String name;
	public final RoomVisibility visibility;
	public final String joinCode;
	public final String hostClientId;
	public final List<String> memberClientIds = new ArrayList<>();

	public LobbyRoom(String name, RoomVisibility visibility, String hostClientId) {
		this.roomId = UUID.randomUUID().toString();
		this.name = name;
		this.visibility = visibility;
		this.hostClientId = hostClientId;
		this.joinCode = visibility == RoomVisibility.PRIVATE ? JoinCodeGenerator.newCode() : null;
		memberClientIds.add(hostClientId);
	}

	public boolean isFull() {
		return memberClientIds.size() >= LobbyConfig.MAX_PLAYERS_PER_ROOM;
	}

	public RoomSnapshot toSnapshot() {
		boolean joinable = !isFull();
		return new RoomSnapshot(roomId, name, visibility, null, memberClientIds.size(),
				LobbyConfig.MAX_PLAYERS_PER_ROOM, joinable);
	}

	public RoomSnapshot toSnapshot(String hostNick) {
		RoomSnapshot s = toSnapshot();
		s.hostNick = hostNick;
		return s;
	}
}
