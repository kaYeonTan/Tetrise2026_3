package kr.ac.jbnu.se.tetris.network.lobby;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 서버 권위: 동시 방 개수·정원·입장 코드 검증. */
public class RoomRegistry {

	private final Map<String, LobbyRoom> roomsById = new LinkedHashMap<>();

	public int roomCount() {
		return roomsById.size();
	}

	public Optional<LobbyRoom> find(String roomId) {
		return Optional.ofNullable(roomsById.get(roomId));
	}

	public List<LobbyRoom> allRooms() {
		return new ArrayList<>(roomsById.values());
	}

	public enum CreateResult {
		OK, ROOM_CAP, INVALID_NAME
	}

	public static final class CreateOutcome {
		public final CreateResult result;
		public final LobbyRoom room;

		CreateOutcome(CreateResult result, LobbyRoom room) {
			this.result = result;
			this.room = room;
		}
	}

	public CreateOutcome createRoom(String name, RoomVisibility visibility, String hostClientId) {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.isEmpty() || trimmed.length() > 32) {
			return new CreateOutcome(CreateResult.INVALID_NAME, null);
		}
		if (roomsById.size() >= LobbyConfig.MAX_ROOMS) {
			return new CreateOutcome(CreateResult.ROOM_CAP, null);
		}
		LobbyRoom room = new LobbyRoom(trimmed, visibility, hostClientId);
		roomsById.put(room.roomId, room);
		return new CreateOutcome(CreateResult.OK, room);
	}

	public enum JoinResult {
		OK, NOT_FOUND, FULL, WRONG_CODE, ALREADY_IN_ROOM
	}

	public JoinResult join(String roomId, String joinCode, String clientId) {
		LobbyRoom room = roomsById.get(roomId);
		if (room == null) {
			return JoinResult.NOT_FOUND;
		}
		if (room.memberClientIds.contains(clientId)) {
			return JoinResult.ALREADY_IN_ROOM;
		}
		if (room.isFull()) {
			return JoinResult.FULL;
		}
		if (room.visibility == RoomVisibility.PRIVATE) {
			if (joinCode == null || !joinCode.trim().equalsIgnoreCase(room.joinCode)) {
				return JoinResult.WRONG_CODE;
			}
		}
		room.memberClientIds.add(clientId);
		return JoinResult.OK;
	}

	public void leave(String clientId) {
		List<String> empty = new ArrayList<>();
		for (Map.Entry<String, LobbyRoom> e : roomsById.entrySet()) {
			LobbyRoom room = e.getValue();
			room.memberClientIds.remove(clientId);
			if (room.memberClientIds.isEmpty()) {
				empty.add(e.getKey());
			}
		}
		for (String id : empty) {
			roomsById.remove(id);
		}
	}

	/** 멤버와 관계없이 방을 목록에서 제거 (온라인 세션 종료 시). */
	public void removeRoom(String roomId) {
		if (roomId != null) {
			roomsById.remove(roomId);
		}
	}

	public Optional<LobbyRoom> roomOfClient(String clientId) {
		for (LobbyRoom room : roomsById.values()) {
			if (room.memberClientIds.contains(clientId)) {
				return Optional.of(room);
			}
		}
		return Optional.empty();
	}
}
