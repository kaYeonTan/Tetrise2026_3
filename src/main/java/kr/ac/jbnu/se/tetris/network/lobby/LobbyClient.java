package kr.ac.jbnu.se.tetris.network.lobby;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/** 게임 클라이언트용 로비 API (UI·GamePanel과 분리). */
public class LobbyClient implements AutoCloseable {

	private LobbyClientConnection conn;
	private String clientId;
	private String nick;

	public void setPushHandler(Consumer<LobbyMessage> handler) {
		if (conn != null) {
			conn.setPushHandler(handler);
		}
	}

	public void connect(String host, int port, String displayNick) throws IOException {
		closeQuietly();
		conn = new LobbyClientConnection(host, port);
		LobbyMessage hello = LobbyMessage.of(LobbyMessageType.HELLO);
		hello.nick = displayNick == null ? "Guest" : displayNick.trim();
		if (hello.nick.isEmpty()) {
			hello.nick = "Guest";
		}
		LobbyMessage resp = conn.request(hello);
		expect(resp, LobbyMessageType.HELLO_OK);
		clientId = resp.clientId;
		nick = resp.nick;
	}

	public List<RoomSnapshot> listRooms() throws IOException {
		LobbyMessage resp = conn.request(LobbyMessage.of(LobbyMessageType.LIST_ROOMS));
		expect(resp, LobbyMessageType.ROOM_LIST);
		return resp.rooms;
	}

	public LobbyMessage createRoom(String roomName, RoomVisibility visibility) throws IOException {
		LobbyMessage req = LobbyMessage.of(LobbyMessageType.CREATE_ROOM);
		req.name = roomName;
		req.visibility = visibility.name();
		LobbyMessage resp = conn.request(req);
		if (LobbyMessageType.ERROR.name().equals(resp.type)) {
			return resp;
		}
		expect(resp, LobbyMessageType.ROOM_CREATED);
		return resp;
	}

	public LobbyMessage joinRoom(String roomId, String joinCodeOrNull) throws IOException {
		LobbyMessage req = LobbyMessage.of(LobbyMessageType.JOIN_ROOM);
		req.roomId = roomId;
		req.joinCode = joinCodeOrNull;
		LobbyMessage resp = conn.request(req);
		if (LobbyMessageType.ERROR.name().equals(resp.type)) {
			return resp;
		}
		expect(resp, LobbyMessageType.JOINED);
		return resp;
	}

	public void leaveRoom() throws IOException {
		LobbyMessage resp = conn.request(LobbyMessage.of(LobbyMessageType.LEAVE_ROOM));
		expect(resp, LobbyMessageType.LEFT);
	}

	/** 방에 없으면 {@code null}. 방장+비밀방이면 {@code joinCode} 포함. */
	public LobbyMessage fetchMyRoom() throws IOException {
		LobbyMessage resp = conn.request(LobbyMessage.of(LobbyMessageType.MY_ROOM));
		if (LobbyMessageType.LEFT.name().equals(resp.type) && "NOT_IN_ROOM".equals(resp.message)) {
			return null;
		}
		expect(resp, LobbyMessageType.JOINED);
		return resp;
	}

	public String getNick() {
		return nick;
	}

	public String getClientId() {
		return clientId;
	}

	public boolean isConnected() {
		return conn != null;
	}

	public LobbyMessage startGame() throws IOException {
		return startGameInternal(LobbyMessage.of(LobbyMessageType.START_GAME));
	}

	public LobbyMessage resumeCoop(String coopId, long seed, String snapshotJson) throws IOException {
		LobbyMessage req = LobbyMessage.of(LobbyMessageType.RESUME_COOP);
		req.coopId = coopId;
		req.seed = seed;
		req.snapshotJson = snapshotJson;
		return startGameInternal(req);
	}

	private LobbyMessage startGameInternal(LobbyMessage req) throws IOException {
		LobbyMessage resp = conn.request(req);
		if (LobbyMessageType.ERROR.name().equals(resp.type)) {
			return resp;
		}
		expect(resp, LobbyMessageType.START_GAME_OK);
		return resp;
	}

	public void sendPlayerAction(LobbyMessage req) throws IOException {
		conn.sendFireAndForget(req);
	}

	public void sendCoopSnapshot(LobbyMessage req) throws IOException {
		conn.sendFireAndForget(req);
	}

	public LobbyMessage requestCoopSave(LobbyMessage req) throws IOException {
		LobbyMessage resp = conn.request(req);
		if (LobbyMessageType.ERROR.name().equals(resp.type)) {
			return resp;
		}
		expect(resp, LobbyMessageType.COOP_SAVE_OK);
		return resp;
	}

	public LobbyClientConnection getConnection() {
		return conn;
	}

	private static void expect(LobbyMessage resp, LobbyMessageType type) throws IOException {
		if (resp == null || !type.name().equals(resp.type)) {
			throw new IOException("Unexpected response: " + (resp == null ? "null" : resp.type));
		}
	}

	private void closeQuietly() {
		if (conn != null) {
			try {
				conn.close();
			} catch (IOException ignored) {
			}
			conn = null;
		}
	}

	@Override
	public void close() {
		closeQuietly();
	}
}
